/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 */
package org.knime.core.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.knime.core.data.container.filter.predicate.TypedColumn.boolCol;
import static org.knime.core.data.container.filter.predicate.TypedColumn.intCol;
import static org.knime.core.data.container.filter.predicate.TypedColumn.longCol;
import static org.knime.core.data.container.filter.predicate.FilterPredicate.custom;
import static org.knime.core.data.container.filter.predicate.FilterPredicate.equal;
import static org.knime.core.data.container.filter.predicate.FilterPredicate.greaterOrEqual;
import static org.knime.core.data.container.filter.predicate.FilterPredicate.lesserOrEqual;

import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import org.junit.Test;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.Buffer;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.filter.TableFilter;
import org.knime.core.data.container.storage.AbstractTableStoreReader;
import org.knime.core.data.container.storage.AbstractTableStoreReader.TableStoreCloseableRowIterator;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.filestore.internal.NotInWorkflowDataRepository;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.SingleNodeContainer;
import org.knime.core.node.workflow.virtual.parchunk.VirtualParallelizedChunkPortObjectInNodeFactory;

/**
 * Unit test for {@link BufferedDataTable#filter(TableFilter)}). Tests that no invalid filter can be passed to the
 * method and that correctly filtered {@link CloseableRowIterator} are retrieved from the table store or memory (via
 * FromListIterator).
 *
 * @author Marc Bux, KNIME GmbH, Berlin, Germany
 */
public class BufferedDataTableIteratorWithFilterTest {

    private static final DataTableSpec SPEC =
        new DataTableSpec(new DataColumnSpecCreator("int", IntCell.TYPE).createSpec(),
            new DataColumnSpecCreator("string", StringCell.TYPE).createSpec(),
            new DataColumnSpecCreator("long", LongCell.TYPE).createSpec(),
            new DataColumnSpecCreator("double", DoubleCell.TYPE).createSpec(),
            new DataColumnSpecCreator("boolean", BooleanCell.TYPE).createSpec());

    // keep only rows with an index that is even and between 10 and 20 (i.e. 10, 12, 14, 16, 18, 20)
    private static final TableFilter FILTER_MOST = ((new TableFilter.Builder()).withFilterPredicate(
        greaterOrEqual(intCol(0), 10).and(lesserOrEqual(longCol(2), 20l)).and(equal(boolCol(4), false))))
            .withFromRowIndex(13).withToRowIndex(17).build();

    private static final NodeProgressMonitor PROGRESS = new DefaultNodeProgressMonitor();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final ExecutionContext EXEC = new ExecutionContext(PROGRESS,
        new Node((NodeFactory)new VirtualParallelizedChunkPortObjectInNodeFactory(new PortType[0])),
        SingleNodeContainer.MemoryPolicy.CacheSmallInMemory, NotInWorkflowDataRepository.newInstance());

    private static BufferedDataTable createTable(final int rowCount, final boolean keepInMemory) {
        final DataRow[] rows = IntStream.range(0, rowCount)
            .mapToObj(i -> new DefaultRow(new RowKey(Integer.toString(i)), new IntCell(i),
                new StringCell(Integer.toString(i)), new LongCell(i), new DoubleCell(i),
                i % 2 == 1 ? BooleanCell.TRUE : BooleanCell.FALSE))
            .toArray(DataRow[]::new);

        final BufferedDataContainer cont = EXEC.createDataContainer(SPEC, true, keepInMemory ? Integer.MAX_VALUE : 0);
        // write the data
        for (final DataRow r : rows) {
            cont.addRowToTable(r);
        }

        cont.close();
        return cont.getTable();
    }

    /**
     * Tests that {@link TableFilter TableFilters} are correctly applied to {@link Buffer Buffers} holding their table
     * in memory.
     */
    @Test
    public void testFromListIterator() {
        BufferedDataTable table = createTable(100, true);

        try (final CloseableRowIterator rowIt = table.filter(FILTER_MOST).iterator()) {
            assertTrue(rowIt.hasNext());
            assertEquals("14", rowIt.next().getKey().getString());
            assertTrue(rowIt.hasNext());
            assertEquals("16", rowIt.next().getKey().getString());
            assertFalse(rowIt.hasNext());
        }
    }

    /**
     * Tests that {@link TableFilter TableFilters} are correctly applied to {@link Buffer Buffers} obtaining their table
     * via a {@link TableStoreCloseableRowIterator} provided by an {@link AbstractTableStoreReader}.
     */
    @Test
    public void testTableStoreCloseableRowIterator() {
        BufferedDataTable table = createTable(100, false);

        try (final CloseableRowIterator rowIt = table.filter(FILTER_MOST).iterator()) {
            assertTrue(rowIt.hasNext());
            assertEquals("14", rowIt.next().getKey().getString());
            assertTrue(rowIt.hasNext());
            assertEquals("16", rowIt.next().getKey().getString());
            assertFalse(rowIt.hasNext());
        }
    }

    /**
     * Tests that {@link TableFilter TableFilters} filtering all rows are correctly applied to {@link Buffer Buffers}
     * holding their table in memory.
     */
    @Test(expected = NoSuchElementException.class)
    public void testFilterAll() {
        BufferedDataTable table = createTable(100, true);

        TableFilter filter = TableFilter.filterRows(custom(intCol(0), i -> false));

        try (final CloseableRowIterator rowIt = table.filter(filter).iterator()) {
            assertFalse(rowIt.hasNext());
            rowIt.next();
        }
    }

    /**
     * Tests that {@link TableFilter TableFilters} filtering no rows are correctly applied to {@link Buffer Buffers}
     * holding their table in memory.
     */
    @Test
    public void testFilterNone() {
        BufferedDataTable table = createTable(100, true);

        TableFilter filter = TableFilter.filterRows(custom(intCol(0), i -> true));

        try (final CloseableRowIterator rowIt1 = table.filter(filter).iterator();
                CloseableRowIterator rowIt2 = table.iterator()) {
            while (rowIt1.hasNext() && rowIt2.hasNext()) {
                assertEquals(rowIt1.next(), rowIt2.next());
            }
            assertEquals(rowIt1.hasNext(), rowIt2.hasNext());
        }
    }

}
