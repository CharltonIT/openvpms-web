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

package org.openvpms.web.app.workflow.scheduling;

import org.openvpms.archetype.rules.workflow.AppointmentQuery;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.workflow.WorkflowQuery;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.AbstractResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Queries <em>act.customerAppointment</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerAppointmentQuery extends WorkflowQuery<ObjectSet> {

    /**
     * The appointment archetype short name.
     */
    private static final String CUSTOMER_APPOINTMENT
            = "act.customerAppointment";


    /**
     * Constructs a new <tt>CustomerAppointmentQuery</tt>.
     *
     * @param schedule the schedule
     */
    public CustomerAppointmentQuery(Party schedule) {
        super(schedule, "schedule", "participation.schedule",
              new String[]{CUSTOMER_APPOINTMENT}, ObjectSet.class);
        // Set the date to the last date unless null
        if (GlobalContext.getInstance().getScheduleDate() != null) {
        	setDate(GlobalContext.getInstance().getScheduleDate());
        }
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return a new result set
     */
    @Override
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        return new ActObjectResultSet();
    }

    private class ActObjectResultSet
            extends AbstractResultSet<ObjectSet> {

        public ActObjectResultSet() {
            super(ArchetypeQuery.ALL_RESULTS);
        }

        /**
         * Returns the total number of results matching the query criteria.
         * For complex queries, this operation can be expensive. If an exact
         * count is not required, use {@link #getEstimatedResults()}.
         *
         * @return the total number of results
         */
        public int getResults() {
            return 0;
        }

        /**
         * Returns an estimation of the total no. of results matching the query
         * criteria.
         *
         * @return an estimation of the total no. of results
         */
        public int getEstimatedResults() {
            return 0;
        }

        /**
         * Determines if the estimated no. of results is the actual total, i.e
         * if {@link #getEstimatedResults()} would return the same as
         * {@link #getResults()}, and {@link #getEstimatedPages()} would return
         * the same as {@link #getPages()}.
         *
         * @return <tt>true</tt> if the estimated results equals the actual no.
         *         of results
         */
        public boolean isEstimatedActual() {
            return true;
        }

        /**
         * Sorts the set. This resets the iterator.
         *
         * @param sort the sort criteria. May be <code>null</code>
         */
        public void sort(SortConstraint[] sort) {
        }

        /**
         * Determines if the node is sorted ascending or descending.
         *
         * @return <code>true</code> if the node is sorted ascending or no sort
         *         constraint was specified; <code>false</code> if it is sorted
         *         descending
         */
        public boolean isSortedAscending() {
            return true;
        }

        /**
         * Returns the sort criteria.
         *
         * @return the sort criteria. Never null
         */
        public SortConstraint[] getSortConstraints() {
            return new SortConstraint[0];
        }

        /**
         * Determines if duplicate results should be filtered.
         *
         * @param distinct if true, remove duplicate results
         */
        public void setDistinct(boolean distinct) {
        }

        /**
         * Determines if duplicate results should be filtered.
         *
         * @return <code>true</code> if duplicate results should be removed;
         *         otherwise <code>false</code>
         */
        public boolean isDistinct() {
            return true;
        }

        /**
         * Returns the specified page.
         *
         * @param page
         * @return the page corresponding to <code>firstResult</code>, or
         *         <code>null</code> if none exists
         */
        protected IPage<ObjectSet> get(int page) {
            Party schedule = (Party) IMObjectHelper.getObject(getEntityId());
            User clinician = (User) IMObjectHelper.getObject(getClinician());
            AppointmentQuery query = new AppointmentQuery();
            query.setSchedule(schedule);
            query.setClinician(clinician);
            query.setDateRange(getFrom(), getTo());
            query.setStatusRange(getStatusRange());
            return query.query();
        }

    }

}
