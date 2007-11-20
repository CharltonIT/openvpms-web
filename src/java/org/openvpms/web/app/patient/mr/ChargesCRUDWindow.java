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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

/**
 * @author tony
 *
 */
public class ChargesCRUDWindow extends ActCRUDWindow {

    /**
     * Reminder and alert shortnames supported by the workspace.
     */
    private static final String[] SHORT_NAMES = {"act.customerAccountInvoiceItem",
                                                 "act.customerAccountCreditItem"};

    /**
     * Create a new <code>ChargesCRUDWindow</code>.
     */
    public ChargesCRUDWindow() {
        super(Messages.get("patient.charges.createtype"),
              new ShortNameList(SHORT_NAMES));
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
            buttons.add(getPrintButton());
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(getPrintButton());
    }
    /**
     * Invoked when the 'print' button is pressed.
     * This implementation prints the current rather than
     * the selected item.
     */
    @Override
    protected void onPrint() {
        try {
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }


}
