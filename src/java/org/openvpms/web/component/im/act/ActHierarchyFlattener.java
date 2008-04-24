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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.act;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NotPredicate;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.functor.RelationshipRef;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * This class enables supports the flattening out the first level of an act
 * heirarchy, optionally filtering child acts.
 * e.g, given a heirarchy of:
 * <ul>
 * <li>event1</li>
 * <ul><li>note1</li><li>problem1</li><li>weight1</li></ul>
 * <li>event2</li>
 * <ul><li>note2</li><li>problem2</li></ul>
 * </ul>
 * and filtering out all child acts bar <em>act.patientNote</em> and
 * <em>act.patientClinicalProblem</em>, the following acts would be returned:
 * <tt>event1, note1, problem1, event2, note2, problem2</tt>.
 * <br/>
 * Note that the child acts are ordered on increasing start time.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActHierarchyFlattener<T extends Act> implements Iterable<T> {

    /**
     * The top level acts.
     */
    private Iterable<T> acts;

    /**
     * The predicate to filter relationships.
     */
    private Predicate predicate;

    /**
     * Creates a new <tt>ActHierarchyFlattener</tt>.
     *
     * @param acts the collection of acts
     */
    public ActHierarchyFlattener(Iterable<T>acts) {
        this(acts, (Predicate) null);
    }

    /**
     * Creates a new <tt>ActHeirarchyFlattener</tt>.
     *
     * @param acts       the collection of acts
     * @param shortNames the child short names to include
     */
    public ActHierarchyFlattener(Iterable<T> acts, String[] shortNames) {
        this(acts, shortNames, true);
    }

    /**
     * Creates a new <tt>ActHeirarchyFlattener</tt>.
     *
     * @param acts       the collection of acts
     * @param shortNames the child short names to include/exclude
     * @param include    if <tt>true</tt> include the acts, otherwise exclude
     *                   them
     */
    public ActHierarchyFlattener(Iterable<T> acts, String[] shortNames,
                                 boolean include) {
        this(acts, createIsA(shortNames, include));
    }

    /**
     * Creates a new <tt>ActHeirarchyFlattener</tt>.
     *
     * @param acts      the collection of acts
     * @param predicate the predicate to select act relationships. If null,
     *                  indicates to select all child acts
     */
    public ActHierarchyFlattener(Iterable<T> acts, Predicate predicate) {
        this.acts = acts;
        this.predicate = predicate;
    }

    /**
     * Returns an iterator over the acts.
     *
     * @return a new iterator
     */
    public Iterator<T> iterator() {
        return new ActIterator();
    }

    /**
     * Determines if an act should be included.
     * <p/>
     * This implementation always returns <tt>true</tt>
     *
     * @param act the act
     * @return <tt>true</tt> if the act should be included
     */
    protected boolean include(T act) {
        return true;
    }

    /**
     * Determines if an act should be included, after the child items have
     * been determined.
     * <p/>
     * This implementation always returns <tt>true</tt>
     *
     * @param parent   the top level act
     * @param children the child acts
     * @return <tt>true</tt> if the act should be included
     */
    protected boolean include(T parent, List<T> children) {
        return true;
    }

    /**
     * Determines if a relationship should be included for evaluation.
     *
     * @param relationship the relationship
     * @return <tt>true</tt> if relationship act should be included
     */
    protected boolean include(ActRelationship relationship) {
        return predicate == null || predicate.evaluate(relationship);
    }

    /**
     * Determines if a child act should be included.
     * <p/>
     * This implementation always returns <tt>true</tt>
     *
     * @param child  the child act
     * @param parent the parent act
     */
    protected boolean include(T child, T parent) {
        return true;
    }


    /**
     * Helper to return a predicate that includes/excludes acts based on their
     * short name.
     *
     * @param shortNames the act short names
     * @param include    if <tt>true</tt> include the acts, otherwise exclude
     *                   them
     * @return a new predicate
     */
    private static Predicate createIsA(final String[] shortNames,
                                       boolean include) {
        Predicate result = new IsA(RelationshipRef.TARGET, shortNames);
        return (include) ? result : new NotPredicate(result);
    }

    private class ActIterator implements Iterator<T> {

        /**
         * Iterator over the top level acts.
         */
        private Iterator<T> actIterator;

        /**
         * Iterator over the current top level act's items.
         */
        private Iterator<T> itemsIterator;


        /**
         * Creates a new <tt>ActIterator</tt>.
         */
        public ActIterator() {
            this.actIterator = acts.iterator();
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements.
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            if (itemsIterator == null || !itemsIterator.hasNext()) {
                advance();
            }
            return itemsIterator != null && itemsIterator.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException iteration has no more elements.
         */
        public T next() {
            if (itemsIterator == null || !itemsIterator.hasNext()) {
                if (!advance()) {
                    throw new NoSuchElementException();
                }
            }
            return itemsIterator.next();
        }

        /**
         * Not supported.
         *
         * @throws UnsupportedOperationException if invoked
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Attempts to advance to the next top level act.
         *
         * @return <tt>true</tt> if the advance was successful.
         */
        @SuppressWarnings("unchecked")
        private boolean advance() {
            while (actIterator.hasNext()) {
                T act = actIterator.next();
                if (include(act)) {
                    List<T> result = new ArrayList<T>();
                    result.add(act);
                    List<T> items = new ArrayList<T>();
                    Set<ActRelationship> relationships
                            = act.getSourceActRelationships();
                    for (ActRelationship relationship : relationships) {
                        if (include(relationship)) {
                            T item = (T) IMObjectHelper.getObject(
                                    relationship.getTarget());
                            if (item != null && include(item, act)) {
                                items.add(item);
                            }
                        }
                    }
                    if (include(act, items)) {
                        sortItems(items);
                        result.addAll(items);
                        itemsIterator = result.iterator();
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Sorts act on start time.
         *
         * @param acts the items to sort
         */
        @SuppressWarnings("unchecked")
        private void sortItems(List<T> acts) {
            Transformer transformer = new Transformer() {
                public Object transform(Object input) {
                    Date date = ((Act) input).getActivityStartTime();
                    if (date instanceof Timestamp) {
                        // to avoid ClassCastException when doing compareTo
                        date = new Date(date.getTime());
                    }
                    return date;
                }
            };
            Comparator comparator = ComparatorUtils.transformedComparator(
                    ComparatorUtils.nullHighComparator(null), transformer);
            Collections.sort(acts, comparator);
        }
    }
}
