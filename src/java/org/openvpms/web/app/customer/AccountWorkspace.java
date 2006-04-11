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

package org.openvpms.web.app.customer;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActTableModel;
import org.openvpms.web.resource.util.Messages;


/**
 * Account workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AccountWorkspace extends ActWorkspace {

    /**
     * Construct a new <code>InvoiceWorkspace</code>.
     */
    public AccountWorkspace() {
        super("customer", "account", "party", "party", "customer*");
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("customer.account.createtype");
        return new AccountCRUDWindow(type, "common", "act", "customerAccount*");
    }

    /**
     * Creates a new query.
     *
     * @param customer the customer to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party customer) {
        return new ActQuery(customer, "act", "customerAccountCharges*",
                            "Posted");
    }

    /**
     * Creates a new table model to display acts.
     *
     * @return a new table model.
     */
    @Override
    protected IMObjectTableModel createTableModel() {
        return new ActTableModel(false, true);
    }

}
