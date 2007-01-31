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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Participation editor for customer. This updates {@link GlobalContext#setCustomer}
 * when a customer is selected, and invokes {@link GlobalContext#setPatient} with
 * null.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-07-03 00:50:52Z $
 */
public class CustomerParticipationEditor
        extends AbstractParticipationEditor<Party> {

    /**
     * Construct a new <code>CustomerParticipationEditor</code>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be <code>null</code>
     */
    public CustomerParticipationEditor(Participation participation,
                                       Act parent,
                                       LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, "participation.customer")) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                            + participation.getArchetypeId().getShortName());
        }
        if (participation.getEntity() == null && parent.isNew()) {
            Party customer = context.getContext().getCustomer();
            getEditor().setObject(customer);
        }
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Party> createObjectReferenceEditor(
            Property property) {
        return new AbstractIMObjectReferenceEditor<Party>(
                property, getParent(), getLayoutContext(), true) {

            @Override
            public void setObject(Party object) {
                super.setObject(object);
                ContextHelper.setCustomer(object);
            }
        };
    }
}
