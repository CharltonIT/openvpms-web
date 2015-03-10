/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.act;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.NotPredicate;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.RelationshipRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;


/**
 * Filters one level of an act hierarchy.
 *
 * @author Tim Anderson
 * @see ActHierarchyIterator
 */
public class ActHierarchyFilter<T extends Act> extends ActFilter<T> {

    /**
     * The predicate to filter relationships. May be {@code null}
     */
    private final Predicate predicate;

    /**
     * Determines if items should be sorted on ascending timestamp.
     */
    private boolean sortAscending = true;


    /**
     * Constructs an {@code ActHierarchyFilter}.
     */
    public ActHierarchyFilter() {
        this(null);
    }

    /**
     * Constructs an {@code ActHierarchyFilter}.
     *
     * @param shortNames the act short names
     * @param include    if {@code true} include the acts, otherwise exclude them
     */
    public ActHierarchyFilter(String[] shortNames, boolean include) {
        this(createIsA(shortNames, include));
    }

    /**
     * Constructs an {@code ActHierarchyFilter}.
     *
     * @param predicate a predicate to filter relationships. May be {@code null}
     */
    public ActHierarchyFilter(Predicate predicate) {
        this.predicate = predicate;
    }

    /**
     * Returns the immediate children of an act, after applying filters.
     *
     * @param act  the act
     * @param root the root of the tree
     * @return the immediate children of the act, or an empty list if they have been filtered
     */
    @Override
    public List<T> filter(T act, T root) {
        List<T> result = new ArrayList<T>();
        if (include(act)) {
            List<T> items = getIncludedTargets(act, root);
            items = filter(act, items);
            if (include(act, items)) {
                result.addAll(items);
            }
        }
        return result;
    }

    /**
     * Returns a comparator to sort the children of an act.
     *
     * @param act the parent act
     * @return the comparator to sort the act's children
     */
    @Override
    public Comparator<T> getComparator(T act) {
        return getComparator(sortAscending);
    }

    /**
     * Determine if items should be sorted on ascending timestamp.
     * <p/>
     * Defaults to {@code true}.
     *
     * @param ascending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    public void setSortItemsAscending(boolean ascending) {
        sortAscending = ascending;
    }

    /**
     * Filters relationships.
     *
     * @param act the act
     * @return the filtered relationships
     */
    protected Collection<ActRelationship> getRelationships(T act) {
        if (predicate == null) {
            return act.getSourceActRelationships();
        }
        return getRelationships(act.getSourceActRelationships(), predicate);
    }

    /**
     * Filters relationships using a predicate.
     *
     * @param relationships the relationships to filter
     * @param predicate     the predicate to use
     * @return the filtered relationships
     */
    protected Collection<ActRelationship> getRelationships(Collection<ActRelationship> relationships,
                                                           Predicate predicate) {
        Collection<ActRelationship> result = new ArrayList<ActRelationship>();
        for (ActRelationship relationship : relationships) {
            if (predicate.evaluate(relationship)) {
                result.add(relationship);
            }
        }
        return result;
    }

    /**
     * Determines if an act should be included.
     * <p/>
     * This implementation always returns {@code true}
     *
     * @param act the act
     * @return {@code true} if the act should be included
     */
    protected boolean include(T act) {
        return true;
    }

    /**
     * Filters child acts.
     * <p/>
     * This implementation returns {@code children} unmodified.
     *
     * @param parent   the parent act
     * @param children the child acts
     * @return the filtered acts
     */
    protected List<T> filter(T parent, List<T> children) {
        return children;
    }

    /**
     * Determines if an act should be included, after the child items have
     * been determined.
     * <p/>
     * This implementation always returns {@code true}
     *
     * @param parent   the top level act
     * @param children the child acts
     * @return {@code true} if the act should be included
     */
    protected boolean include(T parent, List<T> children) {
        return true;
    }

    /**
     * Determines if a child act should be included.
     * <p/>
     * This implementation always returns {@code true}
     *
     * @param child  the child act
     * @param parent the parent act
     * @param root   the root act
     * @return {@code true} if the child act should be included
     */
    protected boolean include(T child, T parent, T root) {
        return true;
    }

    /**
     * Returns the included target acts in set of relationships.
     *
     * @param act  the parent act
     * @param root the root act
     * @return the include target acts
     */
    @SuppressWarnings("unchecked")
    protected List<T> getIncludedTargets(T act, T root) {
        List<T> result = new ArrayList<T>();
        Collection<ActRelationship> relationships = getRelationships(act);
        for (Act match : ActHelper.getTargetActs(relationships)) {
            T item = (T) match;
            if (include(item, act, root)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Helper to return a predicate that includes/excludes acts based on their short name.
     *
     * @param shortNames the act short names
     * @param include    if {@code true} include the acts, otherwise exclude them
     * @return a new predicate
     */
    protected static Predicate createIsA(final String[] shortNames, boolean include) {
        Predicate result = new IsA(RelationshipRef.TARGET, shortNames);
        return (include) ? result : new NotPredicate(result);
    }

}
