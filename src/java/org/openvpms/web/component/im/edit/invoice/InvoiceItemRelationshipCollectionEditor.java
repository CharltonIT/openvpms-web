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

package org.openvpms.web.component.im.edit.invoice;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>actRelationship.customerAccountInvoiceItem</em> and
 * <em>actRelationship.customerAccountCreditItem</em> act relationships.
 * Sets an {@link MedicationManager} on {@link CustomerInvoiceItemEditor}
 * instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InvoiceItemRelationshipCollectionEditor
        extends ActRelationshipCollectionEditor {

    /**
     * The medication manager.
     */
    private final MedicationManager medicationMgr = new MedicationManager();


    /**
     * Constructs a new <code>InvoiceItemRelationshipCollectionEditor</code>.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public InvoiceItemRelationshipCollectionEditor(CollectionProperty property,
                                                   Act act,
                                                   LayoutContext context) {
        super(property, act, context);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit <code>object</code>
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        IMObjectEditor editor = super.createEditor(object, context);
        if (editor instanceof CustomerInvoiceItemEditor) {
            ((CustomerInvoiceItemEditor) editor).setMedicationManager(
                    medicationMgr);
        }
        return editor;
    }
}
