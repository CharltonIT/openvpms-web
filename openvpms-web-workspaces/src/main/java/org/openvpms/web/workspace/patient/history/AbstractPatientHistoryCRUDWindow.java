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

package org.openvpms.web.workspace.patient.history;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.component.workspace.AbstractCRUDWindow;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.PatientRecordCRUDWindow;

import java.util.Arrays;

/**
 * CRUD Window for patient history.
 *
 * @author Tim Anderson
 */
public class AbstractPatientHistoryCRUDWindow extends AbstractCRUDWindow<Act> implements PatientRecordCRUDWindow {

    /**
     * The current <em>act.patientClinicalEvent</em>.
     */
    private Act event;

    /**
     * Constructs a {@link AbstractPatientHistoryCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public AbstractPatientHistoryCRUDWindow(Archetypes<Act> archetypes, IMObjectActions<Act> actions, Context context,
                                            HelpContext help) {
        super(archetypes, actions, context, help);
    }

    /**
     * Sets the current patient clinical event.
     *
     * @param event the current event
     */
    public void setEvent(Act event) {
        this.event = event;
    }

    /**
     * Returns the current patient clinical event.
     *
     * @return the current event. May be {@code null}
     */
    public Act getEvent() {
        return event;
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param object the new object
     */
    @Override
    protected void onCreated(final Act object) {
        if (TypeHelper.isA(object, PatientArchetypes.PATIENT_MEDICATION)) {
            ConfirmationDialog dialog = new ConfirmationDialog(Messages.get("patient.record.create.medication.title"),
                                                               Messages.get("patient.record.create.medication.message"),
                                                               getHelpContext().subtopic("newMedication"));
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    AbstractPatientHistoryCRUDWindow.super.onCreated(object);
                }
            });
            dialog.show();
        } else {
            super.onCreated(object);
        }
    }

    /**
     * Helper to concatenate the short names for the target of a relationship with those supplied.
     *
     * @param relationship the relationship archetype short name
     * @param shortNames   the short names to add
     * @return the archetype shortnames
     */
    protected String[] getShortNames(String relationship, String... shortNames) {
        String[] targets = RelationshipHelper.getTargetShortNames(relationship);
        String[] result = Arrays.copyOf(targets, targets.length + shortNames.length);
        System.arraycopy(shortNames, 0, result, targets.length, shortNames.length);
        return result;
    }
}