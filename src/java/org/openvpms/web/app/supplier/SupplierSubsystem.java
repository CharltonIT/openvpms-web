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

package org.openvpms.web.app.supplier;

import org.openvpms.web.app.subsystem.AbstractCRUDWorkspace;
import org.openvpms.web.app.supplier.charge.ChargeWorkspace;
import org.openvpms.web.app.supplier.delivery.DeliveryWorkspace;
import org.openvpms.web.app.supplier.document.SupplierDocumentWorkspace;
import org.openvpms.web.app.supplier.order.OrderWorkspace;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Supplier subsystem.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SupplierSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <tt>SupplierSubsystem</tt>.
     */
    public SupplierSubsystem() {
        super("supplier");
        MailContext context = new SupplierMailContext(GlobalContext.getInstance());

        addWorkspace(new InformationWorkspace(), context);
        addWorkspace(new SupplierDocumentWorkspace(), context);
        addWorkspace(new OrderWorkspace(), context);
        addWorkspace(new DeliveryWorkspace(), context);
        addWorkspace(new ChargeWorkspace(), context);
        addWorkspace(new PaymentWorkspace(), context);
        addWorkspace(new AccountWorkspace(), context);
    }

    /**
     * Adds a workspace, associating it with the specified mail context.
     *
     * @param workspace the workspace to add
     * @param context   the mail context
     */
    private void addWorkspace(AbstractCRUDWorkspace workspace, MailContext context) {
        workspace.setMailContext(context);
        addWorkspace(workspace);
    }

}
