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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.SchedulingHelper;

import java.util.Calendar;
import java.util.Date;


/**
 * Abstract implementation of the {@link AppointmentGrid} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractAppointmentGrid extends AbstractScheduleEventGrid implements AppointmentGrid {

    /**
     * The grid start time, as minutes since midnight.
     */
    private int startMins = DEFAULT_START;

    /**
     * The grid end time, as minutes since midnight.
     */
    private int endMins = DEFAULT_END;

    /**
     * Appointment rules.
     */
    private AppointmentRules rules;

    /**
     * The slot size, in minutes.
     */
    private int slotSize = DEFAULT_SLOT_SIZE;

    /**
     * The default slot size, in minutes.
     */
    protected static final int DEFAULT_SLOT_SIZE = 15;

    /**
     * The default start time, as minutes from midnight.
     */
    protected static final int DEFAULT_START = 8 * 60;

    /**
     * The default end time, as minutes from midnight.
     */
    protected static final int DEFAULT_END = 18 * 60;

    /**
     * The maximum end time, in minutes.
     */
    private static final int MAX_TIME = 24 * 60;


    /**
     * Constructs an {@link AbstractAppointmentGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the appointment date. All appointments in the grid start or end on this date
     * @param startMins    the grid start time, as minutes from midnight
     * @param endMins      the grid end time, as minutes from midnight
     * @param rules        the appointment rules
     */
    public AbstractAppointmentGrid(Entity scheduleView, Date date, int startMins, int endMins, AppointmentRules rules) {
        super(scheduleView, date);
        this.startMins = startMins;
        this.endMins = endMins;
        this.rules = rules;
    }

    /**
     * Returns the no. of minutes from midnight that the grid starts at.
     *
     * @return the minutes from midnight that the grid starts at
     */
    public int getStartMins() {
        return startMins;
    }

    /**
     * Returns the no. of minutes from midnight that the grid ends at.
     *
     * @return the minutes from midnight that the grid ends at
     */
    public int getEndMins() {
        return endMins;
    }

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    public int getSlots() {
        return (endMins - startMins) / slotSize;
    }

    /**
     * Returns the size of each slot, in minutes.
     *
     * @return the slot size, in minutes
     */
    public int getSlotSize() {
        return slotSize;
    }

    /**
     * Returns the no. of slots at an appointment occupies, from the specified
     * slot.
     * <p/>
     * If the appointment begins prior to the slot, the remaining slots will
     * be returned.
     *
     * @param appointment the appointment
     * @param slot        the starting slot
     * @return the no. of slots that the appointment occupies
     */
    public int getSlots(PropertySet appointment, int slot) {
        Date startTime = getStartTime(slot);
        Date endTime = appointment.getDate(ScheduleEvent.ACT_END_TIME);
        int startSlot = getSlot(startTime);
        int endSlot = getSlot(endTime);
        return endSlot - startSlot;
    }

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param slot the slot
     * @return the start time of the specified slot
     */
    public Date getStartTime(int slot) {
        return DateRules.getDate(getDate(), getStartMins(slot), DateUnits.MINUTES);
    }

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the start time of the specified slot
     */
    public Date getStartTime(Schedule schedule, int slot) {
        return getStartTime(slot);
    }

    /**
     * Returns the no. of minutes from midnight that the specified slot starts at.
     *
     * @param slot the slot
     * @return the minutes that the slot starts at
     */
    public int getStartMins(int slot) {
        return startMins + (slot * slotSize);
    }

    /**
     * Returns the hour of the specified slot.
     *
     * @param slot the slot
     * @return the hour, in the range 0..23
     */
    public int getHour(int slot) {
        int slotSize = getSlotSize();
        return (startMins + (slot * slotSize)) / 60;
    }

    /**
     * Determines the availability of a slot for the specified schedule.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the availability of the schedule
     */
    public Availability getAvailability(Schedule schedule, int slot) {
        int mins = getStartMins(slot);
        if (getEvent(schedule, slot) != null) {
            return Availability.BUSY;
        }
        if (mins < schedule.getStartMins() || mins >= schedule.getEndMins()) {
            return Availability.UNAVAILABLE;
        }
        return Availability.FREE;
    }

    /**
     * Determines how many slots are unavailable from the specified slot, for
     * a schedule.
     *
     * @param schedule the schedule
     * @param slot     the starting slot
     * @return the no. of concurrent slots that are unavailable
     */
    public int getUnavailableSlots(Schedule schedule, int slot) {
        int slots = getSlots();
        int i = slot;
        while (i < slots && getAvailability(schedule, i) == Availability.UNAVAILABLE) {
            ++i;
        }
        return i - slot;
    }

    /**
     * Returns the slot that a time falls in.
     *
     * @param time the time
     * @return the slot, or {@code -1} if the time doesn't intersect any slot
     */
    public int getSlot(Date time) {
        int result;
        Date date = getDate();
        Date day = DateRules.getDate(time);
        if (day.compareTo(date) < 0) {
            result = 0;
        } else if (day.compareTo(date) > 0) {
            result = getSlots();
        } else {
            int mins = getSlotMinutes(time, false);
            result = (mins >= startMins) ? (mins - startMins) / slotSize : -1;
        }
        return result;
    }

    /**
     * Returns the slot size of a schedule.
     *
     * @param schedule the schedule
     * @param rules    the appointment rules
     * @return the slot size
     */
    public static int getSlotSize(Party schedule, AppointmentRules rules) {
        int slotSize = rules.getSlotSize(schedule);
        if (slotSize <= 0) {
            slotSize = DEFAULT_SLOT_SIZE;
        }
        return slotSize;
    }

    /**
     * Sets the no. of minutes from midnight that the grid starts at.
     *
     * @param startMins the minutes from midnight that the grid starts at
     */
    protected void setStartMins(int startMins) {
        this.startMins = startMins;
    }

    /**
     * Sets the no. of minutes from midnight that the grid ends at.
     *
     * @param endMins the minutes from midnight that the grid ends at
     */
    protected void setEndMins(int endMins) {
        this.endMins = endMins;
    }

    /**
     * Sets the size of each slot, in minutes.
     *
     * @param slotSize the slot size, in mimnutes
     */
    protected void setSlotSize(int slotSize) {
        this.slotSize = slotSize;
    }

    /**
     * Returns the minutes from midnight for the specified time, rounded up or down to the nearest slot.
     *
     * @param time    the time
     * @param roundUp if {@code true} round up to the nearest slot, otherwise round down
     * @return the minutes from midnight for the specified time
     */
    protected int getSlotMinutes(Date time, boolean roundUp) {
        return SchedulingHelper.getSlotMinutes(time, slotSize, roundUp);
    }

    /**
     * Creates a new {@link Schedule}.
     *
     * @param schedule the schedule
     * @return a new schedule
     */
    protected Schedule createSchedule(Party schedule) {
        EntityBean bean = new EntityBean(schedule);
        Date start = bean.getDate("startTime");
        int startMins;
        int endMins;
        int slotSize = getSlotSize(schedule, rules);

        if (start != null) {
            startMins = getGridMinutes(start);
        } else {
            startMins = DEFAULT_START;
        }

        Date end = bean.getDate("endTime");
        if (end != null) {
            endMins = getGridMinutes(end);
        } else {
            endMins = DEFAULT_END;
        }
        if (endMins < startMins) {
            endMins = startMins;
        }

        return new Schedule(schedule, startMins, endMins, slotSize);
    }

    /**
     * Determines an appointment grid boundary, in minutes from midnight.
     * <p/>
     * This supports minutes in the range 00:00..24:00
     *
     * @param time the time
     * @return the minutes from midnight for {@code time}
     */
    private int getGridMinutes(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int day = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int mins = calendar.get(Calendar.MINUTE);
        int result = (day * 24 * 60) + (hour * 60) + mins;
        if (result < 0 || result > MAX_TIME) {
            result = DEFAULT_START;
        }
        return result;
    }


}
