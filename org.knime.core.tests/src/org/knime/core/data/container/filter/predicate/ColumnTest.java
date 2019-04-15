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
package org.knime.core.data.container.filter.predicate;

import static org.junit.Assert.assertEquals;
import static org.knime.core.data.container.filter.predicate.Column.boolCol;
import static org.knime.core.data.container.filter.predicate.Column.doubleCol;
import static org.knime.core.data.container.filter.predicate.Column.intCol;
import static org.knime.core.data.container.filter.predicate.Column.longCol;
import static org.knime.core.data.container.filter.predicate.Column.rowKey;
import static org.knime.core.data.container.filter.predicate.Column.stringCol;

import org.junit.Test;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.filter.predicate.IndexedColumn.BooleanColumn;
import org.knime.core.data.container.filter.predicate.IndexedColumn.DoubleColumn;
import org.knime.core.data.container.filter.predicate.IndexedColumn.IntColumn;
import org.knime.core.data.container.filter.predicate.IndexedColumn.LongColumn;
import org.knime.core.data.container.filter.predicate.IndexedColumn.StringColumn;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;

/**
 * Unit tests for the {@link Column} class.
 *
 * @author Marc Bux, KNIME GmbH, Berlin, Germany
 */
public class ColumnTest {

    private static final DataRow ROW = new DefaultRow(new RowKey("1"), new IntCell(0), new LongCell(1l),
        new DoubleCell(2d), new StringCell("3"), BooleanCell.TRUE);

    /**
     * Tests the {@link RowKeyColumn}.
     */
    @Test
    public void testRowKey() {
        assertEquals("1", rowKey().getValue(ROW));
    }

    /**
     * Tests the {@link IntColumn}.
     */
    @Test
    public void testIntCol() {
        assertEquals(0, intCol(0).getValue(ROW).intValue());
    }

    /**
     * Tests the {@link LongColumn}.
     */
    @Test
    public void testLongCol() {
        assertEquals(1l, longCol(1).getValue(ROW).longValue());
    }

    /**
     * Tests the {@link DoubleColumn}.
     */
    @Test
    public void testDoubleCol() {
        assertEquals(2d, doubleCol(2).getValue(ROW).doubleValue(), 0d);
    }

    /**
     * Tests the {@link StringColumn}.
     */
    @Test
    public void testStringCol() {
        assertEquals("3", stringCol(3).getValue(ROW));
    }

    /**
     * Tests the {@link BooleanColumn}.
     */
    @Test
    public void testBoolCol() {
        assertEquals(true, boolCol(4).getValue(ROW).booleanValue());
    }

}
