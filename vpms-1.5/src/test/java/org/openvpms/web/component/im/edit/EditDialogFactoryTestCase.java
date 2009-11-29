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

package org.openvpms.web.component.im.edit;

import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.app.customer.charge.CustomerInvoiceEditDialog;


/**
 * {@link org.openvpms.web.component.im.edit.EditDialogFactory} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EditDialogFactoryTestCase extends AbstractAppTest {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * Verifies that a {@link EditDialog} is returned when no other class is configured.
     */
    public void testCreateDefaultDialog() {
        checkCreate(ContactArchetypes.PHONE, EditDialog.class);
    }

    /**
     * Verifies that a {@link CustomerInvoiceEditDialog} is returned when no other class is configured.
     */
    public void testCreateInvoiceEditDialog() {
        checkCreate(CustomerAccountArchetypes.INVOICE, CustomerInvoiceEditDialog.class);
    }

    /**
     * Verifies that a {@link ActEditDialog} is returned for customer account acts.
     */
    public void testCreateActEditDialog() {
        checkCreate(CustomerAccountArchetypes.COUNTER, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.CREDIT, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.DEBIT_ADJUST, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.PAYMENT, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.REFUND, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.INITIAL_BALANCE, ActEditDialog.class);
        checkCreate(CustomerAccountArchetypes.BAD_DEBT, ActEditDialog.class);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        service = ServiceHelper.getArchetypeService();
    }

    /**
     * Verifies that the editor returned by {@link org.openvpms.web.component.im.edit.IMObjectEditorFactory#create}
     * matches that expected.
     *
     * @param shortName name the archetype short name
     * @param type      the expected editor class
     */
    private void checkCreate(String shortName, Class type) {
        LayoutContext context = new DefaultLayoutContext();
        IMObject object = service.create(shortName);
        assertNotNull("Failed to create object with shortname=" + shortName, object);
        IMObjectEditor editor = IMObjectEditorFactory.create(object, context);
        EditDialog dialog = EditDialogFactory.create(editor);
        assertNotNull("Failed to create editor", editor);
        assertEquals(type, dialog.getClass());
    }

}