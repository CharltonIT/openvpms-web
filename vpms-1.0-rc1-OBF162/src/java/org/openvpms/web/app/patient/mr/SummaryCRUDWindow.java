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

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.app.subsystem.AbstractCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD Window for patient summary. Only supports the display of the acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SummaryCRUDWindow extends AbstractCRUDWindow<Act>
        implements PatientRecordCRUDWindow {

    /**
     * The current act.patientClinicalEvent.
     */
    private Act event;

    /**
     * Creates a new <tt>SummaryCRUDWindow</tt>.
     */
    public SummaryCRUDWindow() {
        super(Messages.get("patient.record.createtype"),
              new ShortNameList("act.patientClinicalEvent"));
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
     * Invoked when the edit button is pressed. This edits the current
     * <em>act.patientClinicalEvent</em>.
     */
    @Override
    protected void onEdit() {
        Act event = getEvent();
        if (event != null) {
            // make sure the latest instance is being used.
            Act current = IMObjectHelper.reload(event);
            if (current == null) {
                ErrorDialog.show(Messages.get("imobject.noexist"),
                                 DescriptorHelper.getDisplayName(event));
            } else {
                edit(current);
            }
        }
    }
}