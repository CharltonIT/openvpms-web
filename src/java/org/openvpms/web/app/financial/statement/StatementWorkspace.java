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

package org.openvpms.web.app.financial.statement;

import echopointng.DateField;
import echopointng.GroupBox;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.app.list.ListSelectionModel;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.subsystem.AbstractWorkspace;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.DateFieldFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.ListBoxFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Statement workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StatementWorkspace extends AbstractWorkspace {

    /**
     * Checkbox to indicate if statments should be generated for all customers,
     * or a range of customers.
     */
    private CheckBox allCustomers;

    /**
     * The 'from-customer' label.
     */
    private Label customerFromLabel;

    /**
     * The 'from-customer' field.
     */
    private TextField customerFrom;

    /**
     * The 'to-customer' label.
     */
    private Label customerToLabel;

    /**
     * The 'to-customer' field.
     */
    private TextField customerTo;


    /**
     * Construct a new <code>StatementWorkspace</code>.
     */
    public StatementWorkspace() {
        super("financial", "statement");
    }

    /**
     * Determines if the workspace supports an archetype.
     *
     * @param shortName the archetype's short name
     * @return <code>true</code> if the workspace can handle the archetype;
     *         otherwise <code>false</code>
     */
    public boolean canHandle(String shortName) {
        return false;
    }

    /**
     * Sets the object to be viewed/edited by the workspace.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(IMObject object) {
        // no-op. This workspace doesn't work on individual objects
    }

    /**
     * Returns the object to to be viewed/edited by the workspace.
     *
     * @return the the object. May be <oode>null</code>
     */
    public IMObject getObject() {
        return null;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        SplitPane root = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL,
                "StatementWorkspace.Layout");
        Component heading = super.doLayout();
        root.add(heading);
        Button run = ButtonFactory.create("run", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onRun();
            }
        });
        Row buttons = RowFactory.create("ControlRow", run);
        SplitPane content = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "StatementWorkspace.Layout", buttons);
        doLayout(content);
        root.add(content);
        return root;
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     */
    private void doLayout(Component container) {
        IPage<IMObject> page = ArchetypeQueryHelper.get(
                ArchetypeServiceHelper.getArchetypeService(),
                new String[]{"classification.customerAccountType"}, true, 0,
                ArchetypeQuery.ALL_ROWS);
        List<IMObject> rows = page.getRows();
        ListModel model = new IMObjectListModel(rows, true, false);
        ListBox accountType = ListBoxFactory.create(model);
        accountType.setCellRenderer(new IMObjectListCellRenderer());
        accountType.setSelectionMode(ListSelectionModel.MULTIPLE_SELECTION);
        accountType.setStyleName("StatementWorkspace.AccountTypes");

        Row accountTypeRow = RowFactory.create(accountType);
        // wrap the list in a row as a workaround for render bug in firefox.
        // See OVPMS-239

        DateField statementDate = DateFieldFactory.create();
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        statementDate.getDateChooser().setSelectedDate(calendar);

        CheckBox preview = CheckBoxFactory.create(false);
        CheckBox finalise = CheckBoxFactory.create(true);
        Grid grid = GridFactory.create(2);
        add(grid, "financial.statement.accountType", accountTypeRow);
        add(grid, "financial.statement.statementDate", statementDate);
        add(grid, "financial.statement.preview", preview);
        add(grid, "financial.statement.finalise", finalise);

        allCustomers = CheckBoxFactory.create(
                "financial.statement.allCustomers", true);
        allCustomers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onAllCustomersChanged();
            }
        });
        customerFromLabel
                = LabelFactory.create("financial.statement.customerFrom");
        customerFrom = TextComponentFactory.create();
        customerToLabel = LabelFactory.create("financial.statement.customerTo");
        customerTo = TextComponentFactory.create();

        add(grid, "financial.statement.customerRange",
            createRow(allCustomers,
                      createRow(customerFromLabel, customerFrom),
                      createRow(customerToLabel, customerTo)));

        GroupBox box = GroupBoxFactory.create(grid);
        container.add(box);

        onAllCustomersChanged(); // initialise customer components
    }

    /**
     * Invoked when the 'run' button is pressed.
     */
    private void onRun() {
        String title = Messages.get("financial.statement.run.title");
        String message = Messages.get("financial.statement.run.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
            }
        });
        dialog.show();

    }

    /**
     * Invoked when the 'all customers' checkbox is selected.
     */
    private void onAllCustomersChanged() {
        boolean enabled = !allCustomers.isSelected();
        enable(customerFromLabel, enabled);
        enable(customerFrom, enabled);
        enable(customerTo, enabled);
        enable(customerToLabel, enabled);
    }

    /**
     * Enable/disable a component.
     *
     * @param component the component to update
     * @param enabled   if <code>true</code> enable the component; otherwise
     *                  disable it
     */
    private void enable(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (enabled) {
            component.setForeground(Color.BLACK);
        } else {
            component.setForeground(Color.LIGHTGRAY);
        }
    }

    /**
     * Helper to create a row containing a set of components.
     *
     * @param components the components
     * @return a row containing the components
     */
    private Row createRow(Component ... components) {
        return RowFactory.create("CellSpacing", components);
    }

    /**
     * Helper to add a label and component to a grid.
     *
     * @param grid      the grid
     * @param key       the label key
     * @param component the component
     */
    private void add(Grid grid, String key, Component component) {
        grid.add(LabelFactory.create(key));
        grid.add(component);
    }

}