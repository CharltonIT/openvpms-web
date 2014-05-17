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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.patient.summary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_PARTICIPATION;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.echo.dialog.PopupDialog;
import static org.openvpms.web.echo.dialog.PopupDialog.OK;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.alert.Alert;
import org.openvpms.web.workspace.alert.AlertSummary;
import org.openvpms.web.workspace.customer.estimate.CustomerEstimateQuery;
import org.openvpms.web.workspace.customer.estimate.EstimateViewer;
import org.openvpms.web.workspace.summary.PartySummary;

/**
 * Renders Patient Summary Information.
 *
 * @author Tim Anderson
 */
public class PatientSummary extends PartySummary {

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The reminder rules.
     */
    private final ReminderRules reminderRules;

    /*
     * The party rules
     */
    /**
     * Constructs a {@link PatientSummary}.
     *
     * @param context the context
     * @param help the help context
     */
    public PatientSummary(Context context, HelpContext help) {
        super(context, help);
        rules = ServiceHelper.getBean(PatientRules.class);
        reminderRules = new ReminderRules(ServiceHelper.getArchetypeService(), rules);
    }

    /**
     * Returns summary information for a party.
     * <p/>
     * The summary includes any alerts.
     *
     * @param patient the patient
     * @return a summary component
     */
    protected Component createSummary(Party patient) {
        Component column = ColumnFactory.create();

        List<Component> components = getSummaryComponents(patient);
        for (Component component : components) {
            if (!(component instanceof Grid)) {
                // grid already inset.... ugly TODO
                column.add(ColumnFactory.create("Inset.Small", component));
            } else {
                column.add(component);
            }
        }
        AlertSummary alerts = getAlertSummary(patient);
        if (alerts != null) {
            column.add(ColumnFactory.create("Inset.Small", alerts.getComponent()));
        }
        return ColumnFactory.create("PartySummary", column);
    }

    /**
     * @param patient
     * @return
     */
    protected List<Component> getSummaryComponents(Party patient) {
        List<Component> result = new ArrayList<Component>();
        result.add(getPatientName(patient));
        result.add(getPatientId(patient));
        if (rules.isDeceased(patient)) {
            result.add(getDeceased());
        }

        result.add(getSpecies(patient));
        result.add(getBreed(patient));
        result.add(createSummaryGrid(patient));
        result.add(addRefferalVet(patient));
        return result;
    }

    /**
     * Returns a component that displays the patient name.
     *
     * @param patient the patient
     * @return the patient name
     */
    protected Component getPatientName(Party patient) {
        String name = patient.getName();
        if (rules.isDesexed(patient)) {
            name += " (" + getPatientSex(patient) + " " + Messages.get("patient.desexed") + ")";
        } else {
            name += " (" + getPatientSex(patient) + " " + Messages.get("patient.entire") + ")";
        }
        IMObjectReferenceViewer patientName = new IMObjectReferenceViewer(patient.getObjectReference(), name, true,
                getContext());
        patientName.setStyleName("hyperlink-bold");
        return patientName.getComponent();
    }

    /**
     * @param patient
     * @return
     */
    protected Component getPatientId(Party patient) {
        String idLabeltext = "ID: " + getID(patient);
        Label idLabel = LabelFactory.create();
        idLabel.setText(idLabeltext);
        return idLabel;
    }

    /**
     * Returns a component indicating the patient is deceased.
     *
     * @return the component
     */
    protected Component getDeceased() {
        return LabelFactory.create("patient.deceased", "Patient.Deceased");
    }

    /**
     * Returns a component that displays the patient species.
     *
     * @param patient the patient
     * @return the patient species
     */
    protected Component getSpecies(Party patient) {
        Label species = LabelFactory.create();
        species.setText(getPatientSpecies(patient));
        return species;
    }

    /**
     * Returns a component that displays the patient breed.
     *
     * @param patient the patient
     * @return the patient breed
     */
    protected Component getBreed(Party patient) {
        Label breed = LabelFactory.create();
        breed.setText(getPatientBreed(patient));
        return breed;
    }

    /**
     * Displays a summary of patient information in a grid.
     *
     * @param patient the patient
     * @return the summary grid
     */
    protected Grid createSummaryGrid(Party patient) {
        Grid grid = GridFactory.create(2);

        addReminders(patient, grid);
        addEstimates(patient, grid);
        addAge(patient, grid);
        addWeight(patient, grid);
        addMicrochip(patient, grid);
        return grid;
    }

    /**
     * Displays a summary of patient reminders in a grid.
     *
     * @param patient the patient
     * @param grid the summary grid
     */
    protected void addReminders(final Party patient, Grid grid) {
        Label reminderTitle = LabelFactory.create("patient.reminders");
        Component reminders;
        ReminderRules.DueState due = getDueState(patient);
        if (due == null) {
            reminders = LabelFactory.create("patient.noreminders");
        } else {
            String style = "reminder." + due.toString();
            reminders = ButtonFactory.create(null, style, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onShowReminders(patient);
                }
            });
            reminders = RowFactory.create(reminders);
        }
        grid.add(reminderTitle);
        grid.add(reminders);
    }
    protected void addEstimates(final Party patient, Grid grid) {
        if(hasEstimates(patient)) {
        Component row = grid.getComponent(1);
        grid.remove(1);
        Grid newgrid = GridFactory.create(2);
            String style = "estimate.available";
            Component estimates = ButtonFactory.create(null,style, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onShowEstimates(patient);
                } 
            });
            newgrid.add(row);
            newgrid.add(estimates);
            Component newrow = RowFactory.create(newgrid);
            grid.add(newrow);
         
        }
    }

    /**
     * Displays the patient age in a grid.
     *
     * @param patient the patient
     * @param grid the grid
     */
    protected void addAge(Party patient, Grid grid) {
        Label ageTitle = LabelFactory.create("patient.age");
        Label age = LabelFactory.create();
        age.setText(rules.getPatientAge(patient));
        grid.add(ageTitle);
        grid.add(age);
    }

    /**
     * Displays the patient weight in a grid.
     *
     * @param patient the patient
     * @param grid the grid
     */
    private void addWeight(Party patient, Grid grid) {
        Label weightTitle = LabelFactory.create("patient.weight");
        Label weight = LabelFactory.create();
        weight.setText(getPatientWeight(patient));
        grid.add(weightTitle);
        grid.add(weight);
    }

    /**
     * Displays the patient microchip in a grid.
     *
     * @param patient the patient
     * @param grid the grid
     */
    protected void addMicrochip(Party patient, Grid grid) {
        String identity = rules.getMicrochip(patient);
        if (identity != null) {
            Label microchipTitle = LabelFactory.create("patient.microchip");
            Label microchip = LabelFactory.create();
            microchip.setText(identity);
            grid.add(microchipTitle);
            grid.add(microchip);
        }
    }

    /**
     * Displays the referral vet if indicated otherwise is empty.
     *
     * @param patient
     * @return Grid with referral Vet info
     */
    protected Grid addRefferalVet(Party patient) {
        Grid grid = GridFactory.create(1);
        Party referralVetparty = rules.getReferralVet(patient);
        if (referralVetparty != null) {
            Label referralVetTitle = LabelFactory.create("patient.referralvet");
            Label referralVet = LabelFactory.create();
            referralVet.setText(referralVetparty.getName());
            grid.add(referralVetTitle);
            grid.add(referralVet);
            Component referralPractice = getReferralPractice(referralVetparty);
            if (referralPractice != null) {
            grid.add(referralPractice);
            }
        }
        return grid;
    }

    /**
     * Returns a component displaying the referral practice.
     *
     * @param referralVet the referring vet as a party.
     * @return referral practice hyperlinked.
     */
    protected Component getReferralPractice(Party referralVet) {
        SupplierRules supplierrules = new SupplierRules(ServiceHelper.getArchetypeService());
        final Party referralPractice = supplierrules.getReferralVetPractice(referralVet, new Date());
        if (referralPractice != null) {
            Component referralPracticeName;
            String refpracticenametext = referralPractice.getName();
            referralPracticeName = ButtonFactory.create(refpracticenametext, "hyperlink-bold", new ActionListener(){
              public void onAction(ActionEvent event) {
                    onShowReferralVet(referralPractice);
                } 
                 
            });
            return referralPracticeName;
        } else {
            return null;
        }
    }

    /**
     * Returns the alerts for a party.
     *
     * @param party the party
     * @return the party's alerts
     */
    protected List<Alert> getAlerts(Party party) {
        return queryAlerts(party);
    }

    /**
     * Returns outstanding alerts for a patient.
     *
     * @param patient the patient
     * @param pageSize the no. of alerts to return per page
     * @return the set of outstanding alerts for the patient
     */
    protected ActResultSet<Act> createAlertsResultSet(Party patient, int pageSize) {
        String[] statuses = {ActStatus.IN_PROGRESS};
        ShortNameConstraint archetypes = new ShortNameConstraint(PatientArchetypes.ALERT, true, true);
        ParticipantConstraint[] participants = {new ParticipantConstraint("patient", PATIENT_PARTICIPATION, patient)};

        IConstraint dateRange = QueryHelper.createDateRangeConstraint(new Date());
        // constrain to alerts that intersect today

        return new ActResultSet<Act>(archetypes, participants, dateRange, statuses, false, null, pageSize, null);
    }
    
    /**
     * Returns outstanding acts for a patient.
     *
     * @param patient the patient
     * @param pageSize the no. of alerts to return per page
     * @return the set IN_PROGRESS acts for the patient
     */
    private ActResultSet<Act> createActResultSet(Party patient, int pageSize, String... shortNames) {
        String[] statuses = {ActStatus.IN_PROGRESS};
        ShortNameConstraint archetypes = new ShortNameConstraint(shortNames, true, true);
        ParticipantConstraint[] participants = {new ParticipantConstraint("patient", PATIENT_PARTICIPATION, patient)};
        return new ActResultSet<Act>(archetypes, participants, null, statuses, false, null, pageSize, null);
    }

    /**
     * Returns the highest due state of a patient's reminders.
     *
     * @param patient the patient
     * @return the patient's highest due state
     */
    private ReminderRules.DueState getDueState(Party patient) {
        ActResultSet<Act> reminders = createActResultSet(patient, 20, ReminderArchetypes.REMINDER);
        ResultSetIterator<Act> iterator = new ResultSetIterator<Act>(reminders);
        ReminderRules.DueState result = null;
        while (iterator.hasNext()) {
            ReminderRules.DueState due = reminderRules.getDueState(iterator.next());
            if (due != null) {
                if (result == null || due.compareTo(result) > 0) {
                    result = due;
                }
                if (result == ReminderRules.DueState.OVERDUE) {
                    break;
                }
            }

        }
        return result;
    }

    /**
     * Invoked to show reminders for a patient in a popup.
     *
     * @param patient the patient
     */
    private void onShowReminders(Party patient) {
        PagedIMTable<Act> table = new PagedIMTable<Act>(new ReminderTableModel(getContext(), getHelpContext()),
                getReminders(patient));
        table.getTable().setDefaultRenderer(Object.class, new ReminderTableCellRenderer());
        new ViewerDialog(Messages.get("patient.summary.reminders"), "PatientSummary.ReminderDialog", table);
    }
    
    private void onShowEstimates(Party patient){
         Party customer = rules.getOwner(patient);
         CustomerEstimateQuery query = new CustomerEstimateQuery(customer);
         List<Act> estimates = query.resultList(patient);
         EstimateViewer viewer = new EstimateViewer(estimates, getContext(), getHelpContext());
         viewer.show();
                 }
    private void onShowReferralVet(Party vet) {
        IMObjectViewer view = new IMObjectViewer(vet, createLayoutContext(getContext(), getHelpContext()));
        new ObjectDialog(Messages.get("patient.referralvet"), "PatientSummary.ReferralDialog", view);
    }

    /**
     * Returns the species for a patient.
     *
     * @param patient the patient
     * @return a string representing the patient species
     */
    private String getPatientSpecies(Party patient) {
        return rules.getPatientSpecies(patient);
    }

    /**
     * Returns the breed for a patient.
     *
     * @param patient the patient
     * @return a string representing the patient breed
     */
    private String getPatientBreed(Party patient) {
        return rules.getPatientBreed(patient);
    }

    /**
     * Returns the sex for a patient.
     *
     * @param patient the patient
     * @return a string representing the patient sex
     */
    private String getPatientSex(Party patient) {
        return rules.getPatientSex(patient);
    }

    /**
     * Returns the current weight for a patient.
     *
     * @param patient the patient
     * @return a string representing the patient weight
     */
    private String getPatientWeight(Party patient) {
        String weight = rules.getPatientWeight(patient);
        return (weight != null) ? weight : Messages.get("patient.noweight");
    }

    private String getID(Party patient) {
        return rules.getID(patient);

    }

    /**
     * Returns outstanding reminders for a patient.
     *
     * @param patient the patient
     * @return the set of outstanding reminders for the patient
     */
    private ResultSet<Act> getReminders(Party patient) {
        String[] shortNames = {ReminderArchetypes.REMINDER};
        String[] statuses = {ActStatus.IN_PROGRESS};
        ShortNameConstraint archetypes = new ShortNameConstraint(
                shortNames, true, true);
        ParticipantConstraint[] participants = {
            new ParticipantConstraint("patient", "participation.patient", patient)
        };
        SortConstraint[] sort = {new NodeSortConstraint("endTime", true)};
        return new ActResultSet<Act>(archetypes, participants, null,
                statuses, false, null, 10, sort);
    }
    
     /**
     * Helper to create a layout context where hyperlinks are disabled.
     *
     * @param help the help context
     * @return a new layout context
     */
    private static LayoutContext createLayoutContext(Context context, HelpContext help) {
        LayoutContext result = new DefaultLayoutContext(context, help);
        result.setEdit(true); // hack to disable hyperlinks
        TableComponentFactory factory = new TableComponentFactory(result);
        result.setComponentFactory(factory);
        return result;
    }
    private Boolean hasEstimates(Party patient) {
        Party customer = rules.getOwner(patient);
        CustomerEstimateQuery query = new CustomerEstimateQuery(customer);
        return query.hasEstimates(patient);
    }

    /**
     * Displays a table in popup window.
     */
    private static class ViewerDialog extends PopupDialog {

        /**
         * Constructs a {@code ViewerDialog}.
         *
         * @param title the dialog title
         * @param style the window style
         * @param table the table to display
         */
        public ViewerDialog(String title, String style, PagedIMTable<Act> table) {
            super(title, style, OK);
            setModal(true);
            getLayout().add(ColumnFactory.create("Inset", table));
            show();
        }
        public ViewerDialog(String title, String style) {
            super(title, style, OK);
            setModal(true);
             
        }

    }

    /**
     * Displays a object view in a pop up window
     */
    private static class ObjectDialog extends PopupDialog {

        /**
         * Constructs a {@code ObjectDialog}
         *
         * @param title string The dialog title
         * @param style string the window style
         * @param objectview Object viewer that contains the object to be
         * displayed
         */
        public ObjectDialog(String title, String style, IMObjectViewer objectview) {
            super(title, style, OK);
            setModal(true);
            this.
            getLayout().add(objectview.getComponent());
            show();
        }
    }
    protected static class EstimateTableModel extends AbstractActTableModel {
        
        public EstimateTableModel(Context context, HelpContext help) {
            super(new String[]{EstimateArchetypes.ESTIMATE}, createLayoutContext(context, help));
        }
    }
    private static class ReminderTableModel extends AbstractActTableModel {

        /**
         * Constructs a {@code ReminderTableModel}.
         *
         * @param context the context
         * @param help the help context
         */
        public ReminderTableModel(Context context, HelpContext help) {
            super(new String[]{ReminderArchetypes.REMINDER}, createLayoutContext(context, help));
        }

        /**
         * Returns a list of descriptor names to include in the table.
         *
         * @return the list of descriptor names to include in the table
         */
        @Override
        protected String[] getNodeNames() {
            return new String[]{"reminderType", "endTime", "product"};
        }

    }

}
