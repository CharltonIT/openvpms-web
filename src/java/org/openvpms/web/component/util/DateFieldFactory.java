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

package org.openvpms.web.component.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import echopointng.DateField;

import org.openvpms.web.component.bound.BoundDateField;
import org.openvpms.web.component.edit.Property;


/**
 * Factory for <code>DateFields</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DateFieldFactory extends ComponentFactory {

    /**
     * Creates a new date field with the default style.
     *
     * @return a new date field
     */
    public static DateField create() {
        return init(new DateField());
    }

    /**
     * Creates a new bound date field with the default style.
     *
     * @param property the property to bind
     * @return a new bound date field
     */
    public static DateField create(Property property) {
        return init(new BoundDateField(property));
    }

    /**
     * Initialises a date field.
     *
     * @param date the date field
     * @return the date field
     */
    private static DateField init(DateField date) {
        setDefaults(date.getDateChooser());
        DateFormat edit = DateFormatter.getFormat(true);
        DateFormat view = DateFormatter.getFormat(false);
        DateFormat format = new DelegatingDateFormat(edit, view);
        date.setDateFormat(format);
        return date;
    }

    /**
     * Helper class to enable separate date formats to be used for editing and
     * viewing a <code>DateField</code>. This is required to support display
     * dates with dd/MM/yyyy format, but also parse years correctly for dates
     * specified as dd/MM/yy.
     */
    private static class DelegatingDateFormat extends DateFormat {

        /**
         * The edit format.
         */
        private final DateFormat _edit;

        /**
         * The view format.
         */
        private final DateFormat _view;

        /**
         * Construct a new <code>DelegatingDateFormat</code>
         *
         * @param edit the edit format
         * @param view the view format
         */
        public DelegatingDateFormat(DateFormat edit, DateFormat view) {
            _edit = edit;
            _view = view;
        }

        /**
         * Formats a Date into a date/time string.
         *
         * @param date          a Date to be formatted into a date/time string.
         * @param toAppendTo    the string buffer for the returning date/time
         *                      string.
         * @param fieldPosition keeps track of the position of the field within
         *                      the returned string. On input: an alignment
         *                      field, if desired. On output: the offsets of the
         *                      alignment field. For example, given a time text
         *                      "1996.07.10 AD at 15:08:56 PDT", if the given
         *                      fieldPosition is DateFormat.YEAR_FIELD, the
         *                      begin index and end index of fieldPosition will
         *                      be set to 0 and 4, respectively. Notice that if
         *                      the same time field appears more than once in a
         *                      pattern, the fieldPosition will be set for the
         *                      first occurrence of that time field. For
         *                      instance, formatting a Date to the time string
         *                      "1 PM PDT (Pacific Daylight Time)" using the
         *                      pattern "h a z (zzzz)" and the alignment field
         *                      DateFormat.TIMEZONE_FIELD, the begin index and
         *                      end index of fieldPosition will be set to 5 and
         *                      8, respectively, for the first occurrence of the
         *                      timezone pattern character 'z'.
         * @return the formatted date/time string.
         */
        public StringBuffer format(Date date, StringBuffer toAppendTo,
                                   FieldPosition fieldPosition) {
            return _view.format(date, toAppendTo, fieldPosition);
        }

        /**
         * Parse a date/time string according to the given parse position.  For
         * example, a time text "07/10/96 4:5 PM, PDT" will be parsed into a
         * Date that is equivalent to Date(837039928046).
         * <p/>
         * <p> By default, parsing is lenient: If the input is not in the form
         * used by this object's format method but can still be parsed as a
         * date, then the parse succeeds.  Clients may insist on strict
         * adherence to the format by calling setLenient(false).
         *
         * @param source The date/time string to be parsed
         * @param pos    On input, the position at which to start parsing; on
         *               output, the position at which parsing terminated, or
         *               the start position if the parse failed.
         * @return A Date, or null if the input could not be parsed
         * @see DateFormat#setLenient(boolean)
         */
        public Date parse(String source, ParsePosition pos) {
            return _edit.parse(source, pos);
        }

        /**
         * Parses text from the beginning of the given string to produce a date.
         * The method may not use the entire text of the given string.
         *
         * @param source A <code>String</code> whose beginning should be
         *               parsed.
         * @return A <code>Date</code> parsed from the string.
         * @throws ParseException if the beginning of the specified string
         *                        cannot be parsed.
         */
        @Override
        public Date parse(String source) throws ParseException {
            return _edit.parse(source);
        }

        /**
         * Parses text from a string to produce a <code>Date</code>.
         *
         * @param source A <code>String</code>, part of which should be parsed.
         * @param pos    A <code>ParsePosition</code> object with index and
         *               error index information as described above.
         * @return A <code>Date</code> parsed from the string. In case of error,
         *         returns null.
         * @throws NullPointerException if <code>pos</code> is null.
         */
        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return _edit.parseObject(source, pos);
        }

        /**
         * Set the calendar to be used by this date format.  Initially, the
         * default calendar for the specified or default locale is used.
         *
         * @param newCalendar the new Calendar to be used by the date format
         */
        @Override
        public void setCalendar(Calendar newCalendar) {
            _edit.setCalendar(newCalendar);
            _view.setCalendar(newCalendar);
        }

        /**
         * Gets the calendar associated with this date/time formatter.
         *
         * @return the calendar associated with this date/time formatter.
         */
        @Override
        public Calendar getCalendar() {
            return _edit.getCalendar();
        }

        /**
         * Allows you to set the number formatter.
         *
         * @param newNumberFormat the given new NumberFormat.
         */
        @Override
        public void setNumberFormat(NumberFormat newNumberFormat) {
            _edit.setNumberFormat(newNumberFormat);
            _view.setNumberFormat(newNumberFormat);
        }

        /**
         * Gets the number formatter which this date/time formatter uses to
         * format and parse a time.
         *
         * @return the number formatter which this date/time formatter uses.
         */
        @Override
        public NumberFormat getNumberFormat() {
            return _edit.getNumberFormat();
        }

        /**
         * Sets the time zone for the calendar of this DateFormat object.
         *
         * @param zone the given new time zone.
         */
        @Override
        public void setTimeZone(TimeZone zone) {
            _edit.setTimeZone(zone);
            _view.setTimeZone(zone);
        }

        /**
         * Gets the time zone.
         *
         * @return the time zone associated with the calendar of DateFormat.
         */
        @Override
        public TimeZone getTimeZone() {
            return _edit.getTimeZone();
        }

        /**
         * Specify whether or not date/time parsing is to be lenient.  With
         * lenient parsing, the parser may use heuristics to interpret inputs
         * that do not precisely match this object's format.  With strict
         * parsing, inputs must match this object's format.
         *
         * @param lenient when true, parsing is lenient
         * @see Calendar#setLenient
         */
        @Override
        public void setLenient(boolean lenient) {
            _edit.setLenient(lenient);
        }

        /**
         * Tell whether date/time parsing is to be lenient.
         */
        @Override
        public boolean isLenient() {
            return _edit.isLenient();
        }

    }

}

