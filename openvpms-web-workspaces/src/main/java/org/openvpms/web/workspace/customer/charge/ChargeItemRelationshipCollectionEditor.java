/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.CollectionResultSetFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.mr.Prescriptions;

import java.util.Date;


/**
 * Editor for <em>actRelationship.customerAccountInvoiceItem</em> and
 * <em>actRelationship.customerAccountCreditItem</em> act relationships.
 * Sets a {@link EditorQueue} on {@link CustomerChargeActItemEditor} instances.
 *
 * @author Tim Anderson
 */
public class ChargeItemRelationshipCollectionEditor extends ActRelationshipCollectionEditor {

    /**
     * Last Selected Item Date.
     */
    private Date lastItemDate = null;

    /**
     * The popup editor manager.
     */
    private EditorQueue editorQueue;

    /**
     * The prescriptions.
     */
    private final Prescriptions prescriptions;

    /**
     * The charge context.
     */
    private final ChargeContext chargeContext;


    /**
     * Constructs a {@link ChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public ChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        this(property, act, context, ChargeItemCollectionResultSetFactory.INSTANCE);
    }

    /**
     * Constructs a {@link ChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public ChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context,
                                                  CollectionResultSetFactory factory) {
        super(property, act, context, factory);
        editorQueue = new DefaultEditorQueue(context.getContext());
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE)) {
            prescriptions = new Prescriptions(getCurrentActs(), ServiceHelper.getBean(PrescriptionRules.class));
        } else {
            prescriptions = null;
        }
        chargeContext = new ChargeContext();
    }

    /**
     * Returns the charge context.
     *
     * @return the charge context
     */
    public ChargeContext getChargeContext() {
        return chargeContext;
    }

    /**
     * Sets the popup editor manager.
     *
     * @param queue the popup editor manager. May be {@code null}
     */
    public void setEditorQueue(EditorQueue queue) {
        editorQueue = queue;
    }

    /**
     * Returns the popup editor manager.
     *
     * @return the popup editor manager. May be {@code null}
     */
    public EditorQueue getEditorQueue() {
        return editorQueue;
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        final IMObjectEditor editor = super.createEditor(object, context);
        initialiseEditor(editor);
        return editor;
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        super.remove(object);
        if (prescriptions != null) {
            prescriptions.removeItem((Act) object);
        }
    }

    /**
     * Initialises an editor.
     *
     * @param editor the editor
     */
    protected void initialiseEditor(final IMObjectEditor editor) {
        if (editor instanceof CustomerChargeActItemEditor) {
            CustomerChargeActItemEditor itemEditor = (CustomerChargeActItemEditor) editor;
            itemEditor.setEditorQueue(editorQueue);
            itemEditor.setPrescriptions(prescriptions);
            itemEditor.setChargeContext(chargeContext);
        }

        // Set startTime to to last used value
        if (lastItemDate != null) {
            editor.getProperty("startTime").setValue(lastItemDate);
        }

        // add a listener to store the last used item starttime.
        ModifiableListener startTimeListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                lastItemDate = (Date) editor.getProperty("startTime").getValue();
            }
        };
        editor.getProperty("startTime").addModifiableListener(startTimeListener);
    }

    /**
     * Saves any current edits.
     *
     * @return {@code true} if edits were saved successfully, otherwise {@code false}
     */
    @Override
    protected boolean doSave() {
        boolean result = (prescriptions == null) || prescriptions.save();
        // Need to save prescriptions first, as invoice item deletion can cause StaleObjectStateExceptions otherwise

        if (result) {
            result = super.doSave();
        }
        return result;
    }
}
