/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   Jun 9, 2008 (wiswedel): created
 */
package org.knime.core.node.workflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.knime.core.internal.ReferencedFile;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.Node;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.workflow.WorkflowPersistor.LoadResult;

/**
 * 
 * @author wiswedel, University of Konstanz
 */
final class CopySingleNodeContainerPersistor implements
        SingleNodeContainerPersistor {
    
    private final SingleNodeContainer m_original;
    private final Node m_node;
    
    /**
     * 
     */
    public CopySingleNodeContainerPersistor(
            final SingleNodeContainer original) {
        m_original = original;
        m_node = new Node(m_original.getNode());
    }

    /** {@inheritDoc} */
    @Override
    public Node getNode() {
        if (m_node == null) {
            throw new IllegalStateException(
                    "Copied node is null, call preLoadNodeContainer first");
        }
        return m_node;
    }

    /** {@inheritDoc} */
    @Override
    public List<ScopeObject> getScopeObjects() {
        List<ScopeObject> objs = m_original.getScopeObjectStack()
            .getScopeObjectsOwnedBy(m_original.getID());
        List<ScopeObject> clones = new ArrayList<ScopeObject>(objs.size());
        for (ScopeObject o : objs) {
            clones.add(o.cloneAndUnsetOwner());
        }
        return clones;
    }

    /** {@inheritDoc} */
    @Override
    public NodeContainerMetaPersistor getMetaPersistor() {
        return new CopyNodeContainerMetaPersistor(m_original);
    }

    /** {@inheritDoc} */
    @Override
    public NodeContainer getNodeContainer(final WorkflowManager parent,
            final NodeID id) {
        return new SingleNodeContainer(parent, id, this);
    }

    /** {@inheritDoc} */
    @Override
    public LoadResult loadNodeContainer(
            final Map<Integer, BufferedDataTable> tblRep,
            final ExecutionMonitor exec) throws InvalidSettingsException,
            CanceledExecutionException, IOException {
        return new LoadResult();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResetAfterLoad() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public LoadResult preLoadNodeContainer(final ReferencedFile nodeFileRef,
            final NodeSettingsRO parentSettings)
            throws InvalidSettingsException, CanceledExecutionException,
            IOException {
        return new LoadResult();
    }

}
