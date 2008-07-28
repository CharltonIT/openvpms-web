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

package org.openvpms.web.app.supplier.charge;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.app.supplier.SupplierActCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.util.Archetypes;


/**
 * CRUD window for supplier invoices.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ChargeCRUDWindow extends SupplierActCRUDWindow<FinancialAct> {

    /**
     * Create a new <tt>ChargeCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public ChargeCRUDWindow(Archetypes<FinancialAct> archetypes) {
        super(archetypes);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        enableButtons(buttons, true);
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
            buttons.add(getPostButton());
            buttons.add(getPreviewButton());
        } else {
            buttons.add(getCreateButton());
        }
    }

}