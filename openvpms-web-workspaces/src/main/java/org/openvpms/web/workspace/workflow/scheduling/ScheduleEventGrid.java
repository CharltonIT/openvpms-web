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
 */

package org.openvpms.web.workspace.workflow.scheduling;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Date;
import java.util.List;


/**
 * Represents a grid of schedule events.
 *
 * @author Tim Anderson
 */
public interface ScheduleEventGrid {

    /**
     * Slot availability.
     */
    enum Availability {
        FREE, BUSY, UNAVAILABLE
    }

    /**
     * Returns the schedule view associated with this grid.
     *
     * @return the schedule view
     */
    Entity getScheduleView();

    /**
     * Returns the schedule date.
     *
     * @return the date, excluding any time
     */
    Date getDate();

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    List<Schedule> getSchedules();

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    int getSlots();

    /**
     * Returns the event for the specified schedule and slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the corresponding event, or {@code null} if none is found
     */
    PropertySet getEvent(Schedule schedule, int slot);

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the start time of the specified slot. May be {@code null}
     */
    Date getStartTime(Schedule schedule, int slot);

    /**
     * Determines the availability of a slot for the specified schedule.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the availability of the schedule
     */
    Availability getAvailability(Schedule schedule, int slot);

    /**
     * Determines how many slots are unavailable from the specified slot, for
     * a schedule.
     *
     * @param schedule the schedule
     * @param slot     the starting slot
     * @return the no. of concurrent slots that are unavailable
     */
    int getUnavailableSlots(Schedule schedule, int slot);

    /**
     * Returns the slot that a time falls in.
     *
     * @param time the time
     * @return the slot, or {@code -1} if the time doesn't intersect any slot
     */
    int getSlot(Date time);


}
