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

package org.openvpms.web.app.supplier;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActTableModel;
import org.openvpms.web.resource.util.Messages;


/**
 * Supplier payment workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PaymentWorkspace extends SupplierActWorkspace {

    /**
     * Payment and refund shortnames supported by the workspace.
     */
    private static final String[] SHORT_NAMES = {"act.supplierAccountPayment",
                                                 "act.supplierAccountRefund"};


    /**
     * Construct a new <code>InvoiceWorkspace</code>.
     */
    public PaymentWorkspace() {
        super("supplier", "payment", "party", "party", "supplier*");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("supplier.payment.createtype");
        return new PaymentCRUDWindow(type, SHORT_NAMES);
    }

    /**
     * Creates a new query.
     *
     * @param customer the customer to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party customer) {
        String[] statuses = {"In Progress", "On Hold"};
        return new ActQuery(customer, "supplier", "participation.supplier",
                            SHORT_NAMES, statuses);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        super.onSaved(object, isNew);
        Act act = (Act) object;
        if ("Posted".equals(act.getStatus())) {
            actSelected(null);
        }
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    protected IMObjectTableModel createTableModel() {
        return new ActTableModel(true, true);
    }

}
