/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.system.ServiceHelper;

import java.util.Arrays;


/**
 * Abstract implementation of the {@link ResultSet} interface for result sets
 * that query the {@link IArchetypeService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractArchetypeServiceResultSet<T>
        extends AbstractResultSet<T> {

    /**
     * Additional constraints to associate with the query. May be <tt>null</tt>.
     */
    private final IConstraint constraints;

    /**
     * Determines if duplicate results should be filtered.
     */
    private boolean distinct;

    /**
     * The sort criteria. May be <tt>null</tt>.
     */
    private SortConstraint[] sort;

    private LRUMap cache = new LRUMap();

    private int count = -1;

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(AbstractArchetypeServiceResultSet.class);


    /**
     * Construct a new <tt>AbstractArchetypeServiceResultSet</tt>.
     *
     * @param pageSize the maximum no. of results per page
     * @param sort     the sort criteria. May be <tt>null</tt>
     */
    public AbstractArchetypeServiceResultSet(int pageSize,
                                             SortConstraint[] sort) {
        this(null, pageSize, sort);
    }

    /**
     * Construct a new <tt>AbstractArchetypeServiceResultSet</tt>.
     *
     * @param constraints query constraints. May be <tt>null</tt>
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be <tt>null</tt>
     */
    public AbstractArchetypeServiceResultSet(IConstraint constraints,
                                             int pageSize,
                                             SortConstraint[] sort) {
        super(pageSize);
        this.constraints = constraints;
        setSortConstraint(sort);
    }

    /**
     * Reset the iterator.
     */
    @Override
    public void reset() {
        cache.clear();
        count = -1;
        super.reset();
    }

    /**
     * Returns the total number of results matching the query criteria.
     *
     * @return the total number of results
     */
    public int getResults() {
        if (count == -1) {
            ArchetypeQuery query = createQuery(0, 0);
            query.setCountResults(true);
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            IPage<IMObject> results = service.get(query);
            count = results.getTotalResults();
        }
        return count;
    }

    /**
     * Sort the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    public void sort(SortConstraint[] sort) {
        setSortConstraint(sort);
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <tt>true</tt> if the node is sorted ascending or no sort
     *         constraint was specified; <tt>false</tt> if it is sorted
     *         descending
     */
    public boolean isSortedAscending() {
        return (sort.length == 0 || sort[0].isAscending());
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    public SortConstraint[] getSortConstraints() {
        return sort;
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @param distinct if true, remove duplicate results
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @return <tt>true</tt> if duplicate results should be removed;
     *         otherwise <tt>false</tt>
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Sets the sort criteria.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    protected void setSortConstraint(SortConstraint[] sort) {
        this.sort = (sort != null) ? sort : new SortConstraint[0];
    }

    /**
     * Returns the query constraints.
     *
     * @return the query constraints
     */
    protected IConstraint getConstraints() {
        return constraints;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    protected abstract ArchetypeQuery createQuery();

    /**
     * Returns a new archetype query.
     * This implementation delegates creation to {@link #createQuery},
     * before adding any {@link #getConstraints()} and
     * {@link #getSortConstraints()}.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximun no of results in the page
     * @return a new query
     */
    protected ArchetypeQuery createQuery(int firstResult, int maxResults) {
        ArchetypeQuery query = createQuery();
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        query.setDistinct(isDistinct());
        query.setCountResults(false);
        IConstraint constraints = getConstraints();
        if (constraints != null) {
            query.add(constraints);
        }
        for (SortConstraint sort : getSortConstraints()) {
            query.add(sort);
        }
        return query;
    }

    /**
     * Returns the specified page.
     *
     * @param page the page no.
     * @return the page corresponding to <tt>page</tt>. May be <tt>null</tt>
     */
    @Override
    public IPage<T> getPage(int page) {
        IPage<T> result = (IPage<T>) cache.get(page);
        if (result == null) {
            result = super.getPage(page);
            if (result != null) {
                cache.put(page, result);
            }
        }
        return result;
    }

    /**
     * Returns the specified page.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximun no of results in the page
     * @return the page corresponding to <tt>firstResult</tt>, or
     *         <tt>null</tt> if none exists
     */
    protected IPage<T> getPage(int firstResult, int maxResults) {
        IPage<IMObject> result = null;
        try {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            ArchetypeQuery query = createQuery(firstResult, maxResults);
            String[] nodes = getNodes();
            if (nodes == null || nodes.length == 0) {
                result = service.get(query);
            } else {
                result = service.get(query, Arrays.asList(nodes));
            }
        } catch (OpenVPMSException exception) {
            log.error(exception, exception);
        }
        return convert(result);
    }

    /**
     * Helper to convert a page from one type to another. <strong>Use with
     * caution</strong>: No attempt is made to verify the contents of the page.
     *
     * @param page the page to convert
     * @return the converted page
     */
    @SuppressWarnings("unchecked")
    protected final <K, T> IPage<K> convert(IPage<T> page) {
        return (IPage<K>) page;
    }
}
