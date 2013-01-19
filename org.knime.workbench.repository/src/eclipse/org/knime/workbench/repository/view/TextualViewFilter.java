/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2008 - 2013
 * KNIME.com, Zurich, Switzerland
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.com
 * email: contact@knime.com
 * ---------------------------------------------------------------------
 *
 * History
 *   Jun 1, 2011 (morent): created
 */

package org.knime.workbench.repository.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 *
 * @author Dominik Morent, KNIME.com, Zurich, Switzerland
 *
 */
public abstract class TextualViewFilter extends ViewerFilter {
    private String m_query;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean select(final Viewer viewer, final Object parentElement,
            final Object element) {

        // this means that the filter has been cleared
        if (!hasNonEmptyQuery()) {
            return true;
        }
        // call helper method
        return doSelect(parentElement, element, true);
    }

    /**
     * Determines if an element shall be selected or not.
     *
     * @param parentElement the parent element
     * @param element the element to check for selection
     * @param recursive whether to recurse into elements or not
     * @return <code>true</code> if the element should be selected
     */
    protected abstract boolean doSelect(Object parentElement,
            Object element, boolean recursive) ;

    /**
     *
     * @param test String to test
     * @return <code>true</code> if the test is contained in the m_query
     *         String (ignoring case)
     */
    protected boolean match(final String test) {
        if (test == null) {
            return false;
        }
        return test.toUpperCase().contains(m_query);
    }

    /**
     * Set the query String that is responsible for selecting nodes/categories.
     *
     * @param query The query string
     */
    public void setQueryString(final String query) {
        m_query = query.toUpperCase();
    }

    /**
     * Returns is this filter has a non-empty query, i.e. if item should be
     * filtered out.
     *
     * @return <code>true</code> if a non-empty query exists, <code>false</code>
     *         otherwise
     */
    public boolean hasNonEmptyQuery() {
        return (m_query != null) && (m_query.length() > 0);
    }
}
