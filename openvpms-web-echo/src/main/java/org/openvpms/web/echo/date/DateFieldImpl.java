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

package org.openvpms.web.echo.date;

import echopointng.DateChooser;
import echopointng.DateField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.WebRenderServlet;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * {@code DateField} that supports the entering of relative dates via {@link RelativeDateParser}.
 *
 * @author Tim Anderson
 */
public class DateFieldImpl extends DateField {

    /**
     * Determines if null or empty strings may be entered.
     */
    private boolean allowNulls = false;

    /**
     * Constructs a {@link DateFieldImpl}.
     */
    public DateFieldImpl() {
        DateFormat edit = DateFormatter.getDateFormat(true);
        DateFormat view = DateFormatter.getDateFormat(false);
        DateFormat format = new DelegatingDateFormat(edit, view, null);
        setDateFormat(format);
        setPopUpAlwaysOnTop(true);

        // add an ActionListener to ensure updates happen in a timely fashion
        getTextField().addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
            }
        });

        // register a date chooser that avoids displayedDate updates if the text field has been updated
        // in the same sync. See OVPMS-1408
        DateChooser chooser = new DateChooser() {
            @Override
            public void processInput(String inputName, Object inputValue) {
                if ("displayedDate".equals(inputName)) {
                    Connection connection = WebRenderServlet.getActiveConnection();
                    if (connection != null && "true".equals(connection.getProperty(getDateUpdatedPropertyKey()))) {
                        return;
                    }
                }
                super.processInput(inputName, inputValue);
            }
        };
        chooser.setSelectedDate(chooser.getSelectedDate());
        setDateChooser(chooser);
    }

    /**
     * Determines if null or empty strings may be input.
     * <p/>
     * If {@code true}, null or empty strings may be input, otherwise they will
     * be ignored, and the field will revert to the prior value.
     * <p/>
     * Defaults to {@code false}.
     *
     * @param allow if {@code true} null or empty strings may be input
     */
    public void setAllowNulls(boolean allow) {
        this.allowNulls = allow;
    }

    /**
     * Sets the date to base relative dates on.
     * <p/>
     * Note that this only works when using the default date format.
     *
     * @param date the date. If {@code null}, relative dates will be based on the current date/time
     */
    public void setRelativeDate(Date date) {
        DateFormat format = getDateFormat();
        if (format instanceof DelegatingDateFormat) {
            ((DelegatingDateFormat) format).setRelativeDate(date);
        }
    }

    /**
     * Called to update the calendar selection model from the current text
     * field contents. Only works if {@code isUpdateFromTextField()}
     * currently returns true.
     */
    @Override
    protected void updateDateFromText() {
        if (isUpdateFromTextField()) {
            if (StringUtils.isEmpty(getText()) && allowNulls) {
                getDateChooser().setSelectedDate(null);
            } else {
                super.updateDateFromText();
            }
            updateTextFromDate();
            Connection connection = WebRenderServlet.getActiveConnection();
            if (connection != null) {
                connection.setProperty(getDateUpdatedPropertyKey(), "true");
            }
        }
    }

    /**
     * Returns a unique property name to register that the date has been updated in the current sync.
     *
     * @return a unique property name
     */
    private String getDateUpdatedPropertyKey() {
        return getRenderId() + ".dateUpdated";
    }

    /**
     * Helper class to enable separate date formats to be used for editing and
     * viewing a {@code DateField}. This is required to support display
     * dates with dd/MM/yyyy format, but also parse years correctly for dates
     * specified as dd/MM/yy.
     */
    private static class DelegatingDateFormat extends DateFormat {

        /**
         * The relative date parser.
         */
        private static final RelativeDateParser parser = new RelativeDateParser();

        /**
         * The edit format.
         */
        private final DateFormat edit;

        /**
         * The view format.
         */
        private final DateFormat view;

        /**
         * The date to base relative dates on. May be {@code null},
         */
        private Date date;

        /**
         * Constructs a {@link DelegatingDateFormat}
         *
         * @param edit the edit format
         * @param view the view format
         * @param date the date to base relative dates on. May be {@code null}
         */
        public DelegatingDateFormat(DateFormat edit, DateFormat view, Date date) {
            this.edit = edit;
            this.view = view;
            this.date = date;
        }

        /**
         * Sets the date to base relative dates on.
         *
         * @param date the date. If {@code null}, relative dates will be based on the current date/time
         */
        public void setRelativeDate(Date date) {
            this.date = date;
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
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
            return view.format(date, toAppendTo, fieldPosition);
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
            return edit.parse(source, pos);
        }

        /**
         * Parses text from the beginning of the given string to produce a date.
         * The method may not use the entire text of the given string.
         *
         * @param source A {@code String} whose beginning should be parsed.
         * @return A {@code Date} parsed from the string.
         * @throws ParseException if the beginning of the specified string cannot be parsed.
         */
        @Override
        public Date parse(String source) throws ParseException {
            Date result = parser.parse(source, date);
            if (result == null) {
                result = edit.parse(source);
            }
            return result;
        }

        /**
         * Parses text from a string to produce a {@code Date}.
         *
         * @param source A {@code String}, part of which should be parsed.
         * @param pos    A {@code ParsePosition} object with index and
         *               error index information as described above.
         * @return A {@code Date} parsed from the string. In case of error, returns null.
         * @throws NullPointerException if {@code pos} is null.
         */
        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return edit.parseObject(source, pos);
        }

        /**
         * Set the calendar to be used by this date format.  Initially, the
         * default calendar for the specified or default locale is used.
         *
         * @param newCalendar the new Calendar to be used by the date format
         */
        @Override
        public void setCalendar(Calendar newCalendar) {
            edit.setCalendar(newCalendar);
            view.setCalendar(newCalendar);
        }

        /**
         * Gets the calendar associated with this date/time formatter.
         *
         * @return the calendar associated with this date/time formatter.
         */
        @Override
        public Calendar getCalendar() {
            return edit.getCalendar();
        }

        /**
         * Allows you to set the number formatter.
         *
         * @param newNumberFormat the given new NumberFormat.
         */
        @Override
        public void setNumberFormat(NumberFormat newNumberFormat) {
            edit.setNumberFormat(newNumberFormat);
            view.setNumberFormat(newNumberFormat);
        }

        /**
         * Gets the number formatter which this date/time formatter uses to
         * format and parse a time.
         *
         * @return the number formatter which this date/time formatter uses.
         */
        @Override
        public NumberFormat getNumberFormat() {
            return edit.getNumberFormat();
        }

        /**
         * Sets the time zone for the calendar of this DateFormat object.
         *
         * @param zone the given new time zone.
         */
        @Override
        public void setTimeZone(TimeZone zone) {
            edit.setTimeZone(zone);
            view.setTimeZone(zone);
        }

        /**
         * Gets the time zone.
         *
         * @return the time zone associated with the calendar of DateFormat.
         */
        @Override
        public TimeZone getTimeZone() {
            return edit.getTimeZone();
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
            edit.setLenient(lenient);
        }

        /**
         * Tell whether date/time parsing is to be lenient.
         */
        @Override
        public boolean isLenient() {
            return edit.isLenient();
        }

    }
}
