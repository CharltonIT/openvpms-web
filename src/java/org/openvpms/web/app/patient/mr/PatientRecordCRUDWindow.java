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
 *  $Id: PatientRecordCRUDWindow.java 942 2006-05-30 07:52:45Z tanderson $
 */

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import static org.openvpms.web.app.patient.mr.PatientRecordTypes.RELATIONSHIP_CLINICAL_EVENT_ITEM;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD Window for patient record acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 07:52:45Z $
 */
public abstract class PatientRecordCRUDWindow extends ActCRUDWindow<Act> {

    /**
     * Clinical event item short names.
     */
    private final String[] clinicalEventItems;

    /**
     * The current act.patientClinicalEvent.
     */
    private Act event;


    /**
     * Creates a new <tt>PatientRecordCRUDWindow</tt>.
     *
     * @param shortNames the short names of archetypes that this may create
     */
    public PatientRecordCRUDWindow(ShortNames shortNames) {
        super(Messages.get("patient.record.createtype"), shortNames);
        clinicalEventItems = ActHelper.getTargetShortNames(
                RELATIONSHIP_CLINICAL_EVENT_ITEM);
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
     * @return the current event. May be <tt>null</tt>
     */
    public Act getEvent() {
        return event;
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act
     * @return <code>true</code> if the act can be edited, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean canEdit(Act act) {
        // @todo fix when statuses are sorted out
        return true;
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
        buttons.add(getPrintButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.removeAll();
        if (enable) {
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            buttons.add(getPrintButton());
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Helper to return the short names of acts that may be added to
     * <em>actRelationship.patientClinicalEventItem</em>.
     *
     * @return the short names
     */
    protected String[] getClinicalEventItemShortNames() {
        return clinicalEventItems;
    }

}
