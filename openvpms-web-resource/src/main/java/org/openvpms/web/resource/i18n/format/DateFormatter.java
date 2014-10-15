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

package org.openvpms.web.resource.i18n.format;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.web.resource.i18n.Messages;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 * Date helper methods.
 *
 * @author Tim Anderson
 */
public class DateFormatter {

    /**
     * Date edit pattern.
     */
    private static final String DATE_EDIT_PATTERN;

    /**
     * Date view pattern.
     */
    private static final String DATE_VIEW_PATTERN;

    /**
     * Full date pattern.
     */
    private static final String FULL_DATE_PATTERN;

    /**
     * Day/month date pattern.
     */
    private static final String DAY_MONTH_DATE_PATTERN;

    /**
     * Time edit pattern.
     */
    private static final String TIME_EDIT_PATTERN;

    /**
     * Time view pattern.
     */
    private static final String TIME_VIEW_PATTERN;

    /**
     * Date/time view pattern.
     */
    private static final String DATE_TIME_VIEW_PATTERN;

    /**
     * Date/time to generate a maximum width (in en locales).
     */
    private static final Date WIDE_DATE;


    /**
     * Format a date.
     *
     * @param date the date to format
     * @param edit if {@code true} format the number for editing
     * @return the formatted date
     */
    public static String formatDate(Date date, boolean edit) {
        return getDateFormat(edit).format(date);
    }

    /**
     * Returns a date format.
     *
     * @param edit if {@code true} return a format for editing otherwise return a format for viewing dates
     * @return a date format
     */
    public static DateFormat getDateFormat(boolean edit) {
        DateFormat format;
        Locale locale = Messages.getLocale();
        String pattern = (edit) ? DATE_EDIT_PATTERN : DATE_VIEW_PATTERN;
        if (pattern == null) {
            if (edit) {
                // specify SHORT style when parsing, so that 2 digit years
                // are handled correctly
                format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
            } else {
                format = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            }
        } else {
            format = new SimpleDateFormat(pattern, locale);
        }
        return format;
    }

    /**
     * Returns the full date format for the current locale.
     * <p/>
     * This can be overridden by specifying the <em>date.format.full</em>
     * property in {@code messages.properties}.
     *
     * @return the full date format
     */
    public static DateFormat getFullDateFormat() {
        Locale locale = Messages.getLocale();
        if (FULL_DATE_PATTERN != null) {
            return new SimpleDateFormat(FULL_DATE_PATTERN, locale);
        }
        return DateFormat.getDateInstance(DateFormat.FULL, locale);
    }

    /**
     * Returns the day/month date format for the current locale.
     * <p/>
     * This can be overridden by specifying the <em>date.format.dayMonth</em>
     * property in {@code messages.properties}.
     * <p/>
     * If unspecified, returns {@link #getFullDateFormat()}.
     *
     * @return the day/month date format
     */
    public static DateFormat getDayMonthDateFormat() {
        Locale locale = Messages.getLocale();
        if (DAY_MONTH_DATE_PATTERN != null) {
            return new SimpleDateFormat(DAY_MONTH_DATE_PATTERN, locale);
        }
        return getFullDateFormat();
    }

    /**
     * Format a time.
     *
     * @param time the time to format
     * @param edit if {@code true} format the number for editing
     * @return the formatted date
     */
    public static String formatTime(Date time, boolean edit) {
        return getTimeFormat(edit).format(time);
    }

    /**
     * Returns a time format.
     * <p/>
     * This will use the <em>time.format.edit</em> and <em>time.format.view</em> properties if specified in
     * <em>messages.properties</em>, else it will fall back to those provided by the locale.
     *
     * @param edit if {@code true} return a format for editing otherwise
     *             return a format for viewing dates
     * @return a date format
     */
    public static DateFormat getTimeFormat(boolean edit) {
        DateFormat format;
        Locale locale = Messages.getLocale();
        String pattern = (edit) ? TIME_EDIT_PATTERN : TIME_VIEW_PATTERN;
        if (pattern == null) {
            if (edit) {
                // specify SHORT style when parsing, so that 2 digit years
                // are handled correctly
                format = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
            } else {
                format = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
            }
        } else {
            format = new SimpleDateFormat(pattern, locale);
        }
        return format;
    }

    /**
     * Returns the time format for the specified style and current locale.
     *
     * @param style the style
     * @return the corresponding time format
     */
    public static DateFormat getTimeFormat(int style) {
        return DateFormat.getTimeInstance(style, Messages.getLocale());
    }

    /**
     * Returns a date-time format.
     *
     * @param edit if {@code true} return a format for editing otherwise
     * @return a format for viewing date-times.
     */
    public static DateFormat getDateTimeFormat(boolean edit) {
        DateFormat format;
        Locale locale = Messages.getLocale();
        if (edit) {
            // specify SHORT style for dates when parsing, so that 2 digit years
            // are handled correctly
            format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        } else if (DATE_TIME_VIEW_PATTERN != null) {
            format = new SimpleDateFormat(DATE_TIME_VIEW_PATTERN, locale);
        } else {
            format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
        }
        return format;
    }

    /**
     * Format a date-time.
     *
     * @param dateTime the date-time to format
     * @param edit     if {@code true} format the date-time for editing
     * @return the formatted date
     */
    public static String formatDateTime(Date dateTime, boolean edit) {
        return getDateTimeFormat(edit).format(dateTime);
    }

    /**
     * Formats a date-time, abbreviating it to just the time if the date is today.
     *
     * @param dateTime the date-time to format
     * @return the formatted date-time
     */
    public static String formatDateTimeAbbrev(Date dateTime) {
        String result;
        if (DateRules.compareDateToToday(dateTime) == 0) {
            result = DateFormatter.formatTime(dateTime, false);
        } else {
            result = DateFormatter.formatDateTime(dateTime, false);
        }
        return result;
    }

    /**
     * Helper to determine the no. of characters to display a date format.
     *
     * @param format the format
     * @return the (approximate) no. of characters required to display a date
     *         in the format
     */
    public static int getLength(DateFormat format) {
        return format.format(WIDE_DATE).length();
    }

    /**
     * Formats a time difference in hours and minutes.
     *
     * @param from the start time. May be {@code null}
     * @param to   the end time. May be {@code null}
     * @return the difference between the time, in hours and minutes, or {@code null} if either argument is {@code null}
     */
    public static String formatTimeDiff(Date from, Date to) {
        if (from == null || to == null) {
            return null;
        }
        long diff = to.getTime() - from.getTime();
        if (diff < 0) {
            diff = 0;
        }
        return formatTime(diff);
    }

    /**
     * Format a time in hours and minutes.
     *
     * @param time the time in milliseconds
     * @return the time in hours and minutes
     */
    public static String formatTime(long time) {
        long hours = time / DateUtils.MILLIS_PER_HOUR;
        long mins = (time % DateUtils.MILLIS_PER_HOUR) / DateUtils.MILLIS_PER_MINUTE;
        return Messages.format("time.format.abs", hours, mins);
    }

    /**
     * Parses a time from a string.
     *
     * @param time the time string
     * @return a Date, with just the time portion set
     * @throws ParseException           if the time can't be parsed
     * @throws NumberFormatException    if a hour or minute specification is not numeric
     * @throws IllegalArgumentException if the hour or minute component is invalid
     */
    public static Date parseTime(String time) throws ParseException {
        return parseTime(time, false);
    }

    /**
     * Parses a time from a string.
     *
     * @param time    the time string
     * @param allow24 if {@code true}, allow hours in the range 0..24, else 0..23
     * @return a Date, with just the time portion set
     * @throws ParseException           if the time can't be parsed
     * @throws NumberFormatException    if a hour or minute specification is not numeric
     * @throws IllegalArgumentException if the hour or minute component is invalid
     */
    public static Date parseTime(String time, boolean allow24) throws ParseException {
        Date result;
        DateFormat format = getTimeFormat(true);
        try {
            result = format.parse(time);
        } catch (ParseException exception) {
            if (time.length() <= 2) {
                result = parseHours(time, allow24);
            } else if (time.length() <= 4) {
                result = parseHoursMins(time, allow24);
            } else {
                throw exception;
            }
        }
        return result;
    }

    /**
     * Parse a time from a string, expected to be in the range 0..24.
     *
     * @param value   the string to parse
     * @param allow24 if {@code true}, allow hours in the range 0..24, else 0..23
     * @return the parsed time
     * @throws NumberFormatException    if the string is not a valid no
     * @throws IllegalArgumentException if the hours aren't in the range 0..24
     */
    private static Date parseHours(String value, boolean allow24) {
        int hours = getHours(value, allow24);
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    /**
     * Parses hours from a string.
     *
     * @param value   the string to parse
     * @param allow24 if {@code true}, allow hours in the range 0..24, else 0..23
     * @return the hours from the string
     * @throws NumberFormatException    if the string is not a valid no
     * @throws IllegalArgumentException if the hours aren't in the allowed range
     */
    private static int getHours(String value, boolean allow24) {
        int hours = Integer.parseInt(value);
        if (hours < 0 || (!allow24 && hours > 23) || (allow24 && hours > 24)) {
            throw new IllegalArgumentException(value);
        }
        return hours;
    }

    /**
     * Parse a time from a string, expected to be of the form [H]HMM.
     *
     * @param value   the string to parse
     * @param allow24 if {@code true}, allow hours in the range 0..24, else 0..23
     * @return the parsed time
     * @throws NumberFormatException    if the hours or minutes can't be parsed
     * @throws IllegalArgumentException if the hours or minutes exceed the range
     */
    private static Date parseHoursMins(String value, boolean allow24) {
        String hourPart;
        if (value.length() == 3) {
            hourPart = value.substring(0, 1);
        } else {
            hourPart = value.substring(0, 2);
        }
        int hours = getHours(hourPart, allow24);
        String minPart = value.substring(hourPart.length());
        int mins = Integer.parseInt(minPart);
        if (mins < 0 || mins > 59) {
            throw new IllegalArgumentException("Minutes exceed range 0..59: " + minPart);
        }
        if (hours == 24 && mins > 0) {
            throw new IllegalArgumentException("Maximum time (24:00) exceeded");
        }
        Calendar calendar = new GregorianCalendar();
        calendar.clear(); // get rid of date, timezone info
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, mins);
        return calendar.getTime();
    }

    static {
        String dateEdit = Messages.get("date.format.edit", true);
        DATE_EDIT_PATTERN = (!StringUtils.isEmpty(dateEdit)) ? dateEdit : null;

        String dateView = Messages.get("date.format.view", true);
        DATE_VIEW_PATTERN = (!StringUtils.isEmpty(dateView)) ? dateView : null;

        String fullDate = Messages.get("date.format.full", true);
        FULL_DATE_PATTERN = (!StringUtils.isEmpty(fullDate)) ? fullDate : null;

        String dayMonth = Messages.get("date.format.dayMonth", true);
        DAY_MONTH_DATE_PATTERN = (!StringUtils.isEmpty(dayMonth)) ? dayMonth : null;

        String timeEdit = Messages.get("time.format.edit", true);
        TIME_EDIT_PATTERN = (!StringUtils.isEmpty(timeEdit)) ? timeEdit : null;

        String timeView = Messages.get("time.format.view", true);
        TIME_VIEW_PATTERN = (!StringUtils.isEmpty(timeView)) ? timeView : null;

        String dateTimeView = Messages.get("datetime.format.view", true);
        DATE_TIME_VIEW_PATTERN = (!StringUtils.isEmpty(dateTimeView)) ? dateTimeView : null;

        Calendar calendar = new GregorianCalendar(2006, 12, 30, 12, 59, 59);
        WIDE_DATE = calendar.getTime();
    }

}
