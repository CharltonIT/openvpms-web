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

package org.openvpms.web.app.financial.deposit;

import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.resource.util.Messages;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;

import nextapp.echo2.app.Component;

import java.util.List;


/**
 * Bank Deposit workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-19 07:20:38Z $
 */
public class DepositWorkspace extends ActWorkspace {

    /**
     * Construct a new <code>DepositWorkspace</code>.
     */
    public DepositWorkspace() {
        super("financial", "deposit", "party", "party", "organisationDeposit");
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    @Override
    public void setObject(IMObject object) {
        super.setObject(object);
        Party deposit = (Party) object;
        layoutWorkspace(deposit, getRootComponent());
        initQuery(deposit);
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("financial.deposit.createtype");
        return new DepositCRUDWindow(type, "common", "act", "bankDeposit");
    }

    /**
     * Creates a new query.
     *
     * @param till the till to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party till) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor("act.bankDeposit");
        NodeDescriptor statuses = archetype.getNodeDescriptor("status");
        List<Lookup> lookups = LookupHelper.get(service, statuses);
        ActQuery query = new ActQuery(till, "depositAccount",
                                      "participation.deposit",
                                      "act", "bankDeposit", lookups, null);
        query.setStatus("UnDeposited");
        return query;
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
    }
}