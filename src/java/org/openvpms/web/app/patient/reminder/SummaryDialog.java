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

package org.openvpms.web.app.patient.reminder;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.EnumSet;


/**
 * Displays reminder generation summary statistics in a popup window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-04-11 04:09:07Z $
 */
class SummaryDialog extends PopupDialog {

    /**
     * Constructs a new <tt>SummaryDialog</tt>.
     *
     * @param stats the statistics to display
     */
    public SummaryDialog(Statistics stats) {
        super(Messages.get("patient.reminder.summary.title"), OK);
        setModal(true);
        EnumSet<ReminderEvent.Action> actions = EnumSet.range(
                ReminderEvent.Action.EMAIL, ReminderEvent.Action.PRINT);

        Grid grid = GridFactory.create(2);
        add(grid, ReminderEvent.Action.CANCEL, stats);

        for (Entity reminderType : stats.getReminderTypes()) {
            String text = reminderType.getName();
            add(grid, text, stats.getCount(reminderType, actions));
        }

        for (ReminderEvent.Action action : actions) {
            add(grid, action, stats);
        }
        getLayout().add(ColumnFactory.create("Inset", grid));
    }

    /**
     * Adds a summary line item to a grid.
     *
     * @param grid   the grid
     * @param action the reminder action
     * @param stats  the reminder statistics
     */
    private void add(Grid grid, ReminderEvent.Action action,
                     Statistics stats) {
        String text = Messages.get("patient.reminder.summary."
                + action.name());
        add(grid, text, stats.getCount(action));
    }

    /**
     * Adds a summary line item to a grid.
     *
     * @param grid  the grid
     * @param text  the item text
     * @param count the statistics count
     */
    private void add(Grid grid, String text, int count) {
        Label label = LabelFactory.create();
        label.setText(text);
        Label value = LabelFactory.create();
        value.setText(Integer.toString(count));
        grid.add(label);
        grid.add(value);
    }
}
