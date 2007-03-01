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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import echopointng.DateField;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.ComponentHelper;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActDateRange {

    /**
     * The focus group.
     */
    private final FocusGroup focus;

    /**
     * Indicates if all dates should be queried. If so, the from and to dates
     * are ignored.
     */
    private CheckBox allDates;

    /**
     * The date-from label.
     */
    private Label fromLabel;

    /**
     * The date-from date.
     */
    private DateField from;

    /**
     * The date-to label.
     */
    private Label toLabel;

    /**
     * The date-to date.
     */
    private DateField to;

    /**
     * The component.
     */
    private Component component;

    /**
     * Cell spacing row style.
     */
    private static final String CELLSPACING_STYLE = "CellSpacing";

    /**
     * Row style name.
     */
    private static final String ROW_STYLE = "ControlRow";


    /**
     * Constructs a new <tt>ActDateRange</tt>
     *
     * @param focus the focus group
     */
    public ActDateRange(FocusGroup focus) {
        this.focus = focus;
    }

    /**
     * Returns the 'from' date.
     *
     * @return the 'from' date, or <tt>null</tt> to query all dates
     */
    public Date getFrom() {
        return allDates.isSelected() ? null : getDate(from);
    }

    /**
     * Sets the 'from' date.
     *
     * @param date the 'from' date
     */
    public void setFrom(Date date) {
        setDate(from, date);
    }

    /**
     * Returns the 'to' date.
     *
     * @return the 'to' date, or <tt>null</tt> to query all dates
     */
    public Date getTo() {
        return allDates.isSelected() ? null : getDate(to);
    }

    /**
     * Sets the 'to' date.
     *
     * @param date the 'to' date
     */
    public void setTo(Date date) {
        setDate(to, date);
    }

    /**
     * Renders the component.
     *
     * @return the component
     */
    public Component getComponent() {
        if (component == null) {
            component = doLayout();
        }
        return component;
    }


    /**
     * Lays out the component.
     *
     * @return the component
     */
    protected Component doLayout() {
        allDates = CheckBoxFactory.create("actquery.all", true);
        allDates.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onAllDatesChanged();
            }
        });

        fromLabel = LabelFactory.create("actquery.from");
        from = DateFieldFactory.create();
        from.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onFromChanged();
                    }
                });

        toLabel = LabelFactory.create("actquery.to");
        to = DateFieldFactory.create();
        to.getDateChooser().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        onToChanged();
                    }
                });
        Row startRange = RowFactory.create(
                CELLSPACING_STYLE,
                fromLabel, from,
                toLabel, to);
        Row startRow = RowFactory.create(ROW_STYLE, allDates, startRange);

        onAllDatesChanged();

        focus.add(allDates);
        focus.add(from);
        focus.add(to);
        return startRow;
    }

    /**
     * Invoked when the 'from' date changes. Sets the 'to' date = 'from' if
     * 'from' is greater.
     */
    private void onFromChanged() {
        Date from = getFrom();
        Date to = getTo();
        if (from != null && from.compareTo(to) > 0) {
            setTo(from);
        }
    }

    /**
     * Invoked when the 'to' date changes. Sets the 'from' date = 'to' if
     * 'from' is greater.
     */
    private void onToChanged() {
        Date from = getFrom();
        Date to = getTo();
        if (from != null && from.compareTo(to) > 0) {
            setFrom(to);
        }
    }

    /**
     * Invoked when the 'all dates' check box changes.
     */
    private void onAllDatesChanged() {
        boolean enabled = !allDates.isSelected();
        ComponentHelper.enable(fromLabel, enabled);
        ComponentHelper.enable(from, enabled);
        ComponentHelper.enable(toLabel, enabled);
        ComponentHelper.enable(to, enabled);
    }

    /**
     * Returns the date of the given field.
     *
     * @param field the date field
     * @return the selected date from <code>field</code>
     */
    private Date getDate(DateField field) {
        return field.getDateChooser().getSelectedDate().getTime();
    }

    /**
     * Sets the date of the given field.
     *
     * @param field the date field
     * @param date  the date to set
     */
    private void setDate(DateField field, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        field.getDateChooser().setSelectedDate(calendar);
    }

}
