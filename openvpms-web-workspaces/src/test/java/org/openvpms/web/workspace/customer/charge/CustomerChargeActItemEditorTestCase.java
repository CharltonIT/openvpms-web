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

import nextapp.echo2.app.event.WindowPaneListener;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.discount.DiscountTestHelper;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.checkSavePopup;

/**
 * Tests the {@link CustomerChargeActItemEditor} class.
 *
 * @author Tim Anderson
 */
public class CustomerChargeActItemEditorTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * Tracks errors logged.
     */
    private List<String> errors = new ArrayList<String>();

    /**
     * The context.
     */
    private Context context;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();

        // register an ErrorHandler to collect errors
        ErrorHandler.setInstance(new ErrorHandler() {
            @Override
            public void error(Throwable cause) {
                errors.add(cause.getMessage());
            }

            public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
                errors.add(message);
            }
        });
        context = new LocalContext();
        context.setPractice(getPractice());
        Party location = TestHelper.createLocation();
        context.setLocation(location);
    }

    /**
     * Tests populating an invoice item with a medication.
     */
    @Test
    public void testInvoiceItemMedication() {
        checkInvoiceItem(ProductArchetypes.MEDICATION);
    }

    /**
     * Tests populating an invoice item with a merchandise product.
     */
    @Test
    public void testInvoiceItemMerchandise() {
        checkInvoiceItem(ProductArchetypes.MERCHANDISE);
    }

    /**
     * Tests populating an invoice item with a service product.
     */
    @Test
    public void testInvoiceItemService() {
        checkInvoiceItem(ProductArchetypes.SERVICE);
    }

    /**
     * Tests populating an invoice item with a template product.
     */
    @Test
    public void testInvoiceItemTemplate() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItemWithTemplate(invoice, item);
    }

    /**
     * Tests populating a counter sale item with a medication product.
     */
    @Test
    public void testCounterSaleItemMedication() {
        checkCounterSaleItem(ProductArchetypes.MEDICATION);
    }

    /**
     * Tests populating a counter sale item with a merchandise product.
     */
    @Test
    public void testCounterSaleItemMerchandise() {
        checkCounterSaleItem(ProductArchetypes.MERCHANDISE);
    }

    /**
     * Tests populating a counter sale item with a service product.
     */
    @Test
    public void testCounterSaleItemService() {
        checkCounterSaleItem(ProductArchetypes.SERVICE);
    }

    /**
     * Tests populating a counter sale item with a template product.
     */
    @Test
    public void testCounterSaleItemTemplate() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new BigDecimal(100), customer, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct counterSale = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItemWithTemplate(counterSale, item);
    }

    /**
     * Tests populating a credit item with a medication product.
     */
    @Test
    public void testCreditItemMedication() {
        checkCreditItem(ProductArchetypes.MEDICATION);
    }

    /**
     * Tests populating a credit item with a merchandise product.
     */
    @Test
    public void testCreditItemMerchandise() {
        checkCreditItem(ProductArchetypes.MERCHANDISE);
    }

    /**
     * Tests populating a credit item with a service product.
     */
    @Test
    public void testCreditItemService() {
        checkCreditItem(ProductArchetypes.SERVICE);
    }

    /**
     * Tests populating a credit item with a template product.
     */
    @Test
    public void testCreditItemTemplate() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new BigDecimal(100), customer, null, null,
                                                                          ActStatus.IN_PROGRESS);
        FinancialAct credit = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItemWithTemplate(credit, item);
    }

    /**
     * Verifies that the clinician can be cleared, as a test for OVPMS-1104.
     */
    @Test
    public void testClearClinician() {
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createUser();
        User clinician = TestHelper.createUser();

        // create product1 with reminder and investigation type
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);

        // set up the context
        layout.getContext().setUser(author); // to propagate to acts

        // create the editor
        TestCustomerChargeActItemEditor editor = new TestCustomerChargeActItemEditor(item, charge, layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        // populate quantity, patient, clinician.
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setClinician(clinician);

        // set the product
        editor.setProduct(product);

        // editor should now be valid
        assertTrue(editor.isValid());
        editor.setClinician(null);

        checkSave(charge, editor);

        charge = get(charge);
        item = get(item);
        assertNotNull(charge);
        assertNotNull(item);

        // verify the item matches that expected
        checkItem(item, patient, product, author, null, quantity, unitCost, unitPrice, fixedCost,
                  fixedPrice, discount, tax, total);

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that prices and totals are correct when the customer has tax exemptions.
     */
    @Test
    public void testTaxExemption() {
        addTaxExemption(customer);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createUser();
        User clinician = TestHelper.createUser();

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal discount = BigDecimal.ZERO;

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);

        // set up the context
        layout.getContext().setUser(author); // to propagate to acts

        // create the editor
        TestCustomerChargeActItemEditor editor = new TestCustomerChargeActItemEditor(item, charge, layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        // populate quantity, patient, clinician.
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setClinician(clinician);

        // set the product
        editor.setProduct(product);

        // editor should now be valid
        assertTrue(editor.isValid());
        editor.setClinician(null);

        checkSave(charge, editor);

        charge = get(charge);
        item = get(item);
        assertNotNull(charge);
        assertNotNull(item);

        // verify the item matches that expected
        BigDecimal fixedPriceExTax = new BigDecimal("1.82");
        BigDecimal unitPriceExTax = new BigDecimal("9.09");
        BigDecimal totalExTax = new BigDecimal("20");
        checkItem(item, patient, product, author, null, quantity, unitCost, unitPriceExTax, fixedCost,
                  fixedPriceExTax, discount, BigDecimal.ZERO, totalExTax);

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that prices and totals are correct when the customer has tax exemptions and a service ratio is in place.
     */
    @Test
    public void testTaxExemptionWithServiceRatio() {
        addTaxExemption(customer);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createUser();
        User clinician = TestHelper.createUser();

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal ratio = BigDecimal.valueOf(2); // double the fixed and unit prices

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);
        Entity productType = ProductTestHelper.createProductType("Z Product Type");
        ProductTestHelper.addProductType(product, productType);
        ProductTestHelper.addServiceRatio(context.getLocation(), productType, ratio);

        // set up the context
        layout.getContext().setUser(author); // to propagate to acts

        // create the editor
        TestCustomerChargeActItemEditor editor = new TestCustomerChargeActItemEditor(item, charge, layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        // populate quantity, patient, clinician and product
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setClinician(clinician);
        editor.setProduct(product);

        // editor should now be valid
        assertTrue(editor.isValid());
        checkSave(charge, editor);

        charge = get(charge);
        item = get(item);
        assertNotNull(charge);
        assertNotNull(item);

        // verify the item matches that expected
        BigDecimal fixedPriceExTax = new BigDecimal("1.82").multiply(ratio);
        BigDecimal unitPriceExTax = new BigDecimal("9.09").multiply(ratio);
        BigDecimal totalExTax = new BigDecimal("20").multiply(ratio);
        checkItem(item, patient, product, author, clinician, quantity, unitCost, unitPriceExTax, fixedCost,
                  fixedPriceExTax, discount, BigDecimal.ZERO, totalExTax);

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Tests a product with a 10% discount on an invoice item.
     */
    @Test
    public void testInvoiceItemDiscounts() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);
        checkDiscounts(invoice, item);
    }

    /**
     * Tests a product with a 10% discount on a counter sale item.
     */
    @Test
    public void testCounterSaleItemDiscounts() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new BigDecimal(100), customer, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct counterSale = acts.get(0);
        FinancialAct item = acts.get(1);
        checkDiscounts(counterSale, item);
    }


    /**
     * Tests a product with a 10% discount on a credit item.
     */
    @Test
    public void testCreditItemDiscounts() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new BigDecimal(100), customer, null, null,
                                                                          ActStatus.IN_PROGRESS);
        FinancialAct credit = acts.get(0);
        FinancialAct item = acts.get(1);
        checkDiscounts(credit, item);
    }

    /**
     * Tests a product with a 10% discount where discounts are disabled at the practice location.
     * <p/>
     * The calculated discount should be zero.
     */
    @Test
    public void testDisableDiscounts() {
        IMObjectBean bean = new IMObjectBean(context.getLocation());
        bean.setValue("disableDiscounts", true);

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        User clinician = TestHelper.createClinician();
        User author = TestHelper.createUser();
        Entity discount = DiscountTestHelper.createDiscount(BigDecimal.TEN, true, DiscountRules.PERCENTAGE);
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedCost, fixedPrice, unitCost, unitPrice);
        Party patient = TestHelper.createPatient();
        addDiscount(customer, discount);
        addDiscount(product, discount);

        context.setUser(author);
        context.setClinician(clinician);
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));

        // create the editor
        TestCustomerChargeActItemEditor editor = new TestCustomerChargeActItemEditor(item, charge, layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        if (!TypeHelper.isA(item, CustomerAccountArchetypes.COUNTER_ITEM)) {
            // counter sale items have no patient
            editor.setPatient(patient);
        }
        editor.setProduct(product);
        editor.setQuantity(quantity);

        // editor should now be valid
        assertTrue(editor.isValid());

        checkSave(charge, editor);

        item = get(item);
        // should be no discount
        BigDecimal discount1 = BigDecimal.ZERO;
        BigDecimal tax1 = new BigDecimal("2.00");
        BigDecimal total1 = new BigDecimal("22.00");
        checkItem(item, patient, product, author, clinician, quantity, unitCost, unitPrice, fixedCost,
                  fixedPrice, discount1, tax1, total1);
    }

    /**
     * Checks populating an invoice item with a product.
     *
     * @param productShortName the product archetype short name
     */
    private void checkInvoiceItem(String productShortName) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItem(invoice, item, productShortName);
    }

    /**
     * Checks populating a counter sale item with a product.
     *
     * @param productShortName the product archetype short name
     */
    private void checkCounterSaleItem(String productShortName) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new BigDecimal(100), customer, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct counterSale = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItem(counterSale, item, productShortName);
    }

    /**
     * Checks populating a credit item with a product.
     *
     * @param productShortName the product archetype short name
     */
    private void checkCreditItem(String productShortName) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new BigDecimal(100), customer, null, null,
                                                                          ActStatus.IN_PROGRESS);
        FinancialAct credit = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItem(credit, item, productShortName);
    }

    /**
     * Checks populating a charge item with a product.
     *
     * @param charge           the charge
     * @param item             the charge item
     * @param productShortName the product archetype short name
     */
    private void checkItem(FinancialAct charge, FinancialAct item, String productShortName) {
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();
        User author1 = TestHelper.createUser();
        User author2 = TestHelper.createUser();
        User clinician1 = TestHelper.createUser();
        User clinician2 = TestHelper.createUser();

        // create product1 with reminder and investigation type
        BigDecimal quantity1 = BigDecimal.valueOf(2);
        BigDecimal unitCost1 = BigDecimal.valueOf(5);
        BigDecimal unitPrice1 = BigDecimal.valueOf(10);
        BigDecimal fixedCost1 = BigDecimal.ONE;
        BigDecimal fixedPrice1 = BigDecimal.valueOf(2);
        BigDecimal discount1 = BigDecimal.ZERO;
        BigDecimal tax1 = BigDecimal.valueOf(2);
        BigDecimal total1 = BigDecimal.valueOf(22);
        Product product1 = createProduct(productShortName, fixedCost1, fixedPrice1, unitCost1, unitPrice1);
        Entity reminderType = addReminder(product1);
        Entity investigationType = addInvestigation(product1);
        Entity template = addTemplate(product1);

        // create  product2 with no reminder no investigation type, and a service ratio that doubles the unit and
        // fixed prices
        BigDecimal quantity2 = BigDecimal.ONE;
        BigDecimal unitCost2 = BigDecimal.valueOf(5);
        BigDecimal unitPrice2 = BigDecimal.valueOf(5.5);
        BigDecimal fixedCost2 = BigDecimal.valueOf(0.5);
        BigDecimal fixedPrice2 = BigDecimal.valueOf(5.5);
        BigDecimal discount2 = BigDecimal.ZERO;
        BigDecimal ratio = BigDecimal.valueOf(2);
        BigDecimal tax2 = BigDecimal.valueOf(2);
        BigDecimal total2 = BigDecimal.valueOf(22);

        Product product2 = createProduct(productShortName, fixedCost2, fixedPrice2, unitCost2, unitPrice2);
        Entity productType = ProductTestHelper.createProductType("Z Product Type");
        ProductTestHelper.addProductType(product2, productType);
        ProductTestHelper.addServiceRatio(context.getLocation(), productType, ratio);

        // set up the context
        layout.getContext().setUser(author1); // to propagate to acts
        layout.getContext().setClinician(clinician1);

        // create the editor
        TestCustomerChargeActItemEditor editor = new TestCustomerChargeActItemEditor(item, charge, layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        // register a handler for act popups
        ChargeEditorQueue mgr = new ChargeEditorQueue();
        editor.setEditorQueue(mgr);

        // populate quantity, patient, product. If product1 is a medication, it should trigger a patient medication
        // editor popup
        editor.setQuantity(quantity1);

        if (!TypeHelper.isA(item, CustomerAccountArchetypes.COUNTER_ITEM)) {
            // counter sale items have no patient
            editor.setPatient(patient1);
        }
        editor.setProduct(product1);

        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (TypeHelper.isA(product1, ProductArchetypes.MEDICATION)) {
                // invoice items have a dispensing node
                assertFalse(editor.isValid()); // not valid while popup is displayed
                checkSavePopup(mgr, PatientArchetypes.PATIENT_MEDICATION, false);
                // save the popup editor - should be a medication
            }

            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(mgr, InvestigationArchetypes.PATIENT_INVESTIGATION, false);

            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(mgr, ReminderArchetypes.REMINDER, false);
        }

        // editor should now be valid
        assertTrue(editor.isValid());

        // save it
        checkSave(charge, editor);

        charge = get(charge);
        item = get(item);
        assertNotNull(charge);
        assertNotNull(item);

        // verify the item matches that expected
        checkItem(item, patient1, product1, author1, clinician1, quantity1, unitCost1, unitPrice1, fixedCost1,
                  fixedPrice1, discount1, tax1, total1);
        ActBean itemBean = new ActBean(item);
        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (TypeHelper.isA(product1, ProductArchetypes.MEDICATION)) {
                // verify there is a medication act
                checkMedication(item, patient1, product1, author1, clinician1);
            } else {
                assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
            }

            assertEquals(1, itemBean.getActs(InvestigationArchetypes.PATIENT_INVESTIGATION).size());
            assertEquals(1, itemBean.getActs(ReminderArchetypes.REMINDER).size());
            assertEquals(1, itemBean.getActs("act.patientDocument*").size());

            checkInvestigation(item, patient1, investigationType, author1, clinician1);
            checkReminder(item, patient1, product1, reminderType, author1, clinician1);
            checkDocument(item, patient1, product1, template, author1, clinician1);
        } else {
            // verify there are no medication, investigation, reminder nor document acts
            assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
            assertTrue(itemBean.getActs(InvestigationArchetypes.PATIENT_INVESTIGATION).isEmpty());
            assertTrue(itemBean.getActs(ReminderArchetypes.REMINDER).isEmpty());
            assertTrue(itemBean.getActs("act.patientDocument*").isEmpty());
        }

        // now replace the patient, product, author and clinician
        if (itemBean.hasNode("patient")) {
            editor.setPatient(patient2);
        }
        editor.setProduct(product2);
        editor.setQuantity(quantity2);
        editor.setDiscount(discount2);
        editor.setAuthor(author2);
        if (itemBean.hasNode("clinician")) {
            editor.setClinician(clinician2);
        }

        // should be no more popups. For medication products, the
        assertNull(mgr.getCurrent());  // no new popup - existing medication should update
        assertTrue(editor.isValid());

        // save it
        checkSave(charge, editor);

        item = get(item);
        assertNotNull(item);

        checkItem(item, patient2, product2, author2, clinician2, quantity2, unitCost2, unitPrice2.multiply(ratio),
                  fixedCost2, fixedPrice2.multiply(ratio), discount2, tax2, total2);
        itemBean = new ActBean(item);
        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)
            && TypeHelper.isA(product2, ProductArchetypes.MEDICATION)) {
            // verify there is a medication act. Note that it retains the original author
            checkMedication(item, patient2, product2, author1, clinician2);
        } else {
            // verify there is a medication act
            assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
        }
        assertTrue(itemBean.getActs(InvestigationArchetypes.PATIENT_INVESTIGATION).isEmpty());
        assertTrue(itemBean.getActs(ReminderArchetypes.REMINDER).isEmpty());
        assertTrue(itemBean.getActs("act.patientDocument*").isEmpty());

        // make sure that clinicians can be set to null, as a test for OVPMS-1104
        if (itemBean.hasNode("clinician")) {
            editor.setClinician(null);
            assertTrue(editor.isValid());
            checkSave(charge, editor);

            item = get(item);
            assertNotNull(item);

            checkItem(item, patient2, product2, author2, null, quantity2, unitCost2, unitPrice2.multiply(ratio),
                      fixedCost2, fixedPrice2.multiply(ratio), discount2, tax2, total2);
        }

        editor.setProduct(null);       // make sure nulls are handled
        assertFalse(editor.isValid());

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Checks populating a charge item with a template product.
     * <p/>
     * NOTE: currently, charge items with template products validate correctly, but fail to save.
     * <p/>This is because the charge item relationship editor will only expand templates if the charge item itself
     * is valid - marking the item invalid for having a template would prevent this.
     * TODO - not ideal.
     *
     * @param charge the charge
     * @param item   the charge item
     */
    private void checkItemWithTemplate(FinancialAct charge, FinancialAct item) {
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        context.getContext().setPractice(getPractice());

        Party patient = TestHelper.createPatient();
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        Product product = createProduct(ProductArchetypes.TEMPLATE, fixedCost, fixedPrice, unitCost, unitPrice);
        // costs and prices should be ignored
        User author = TestHelper.createUser();
        User clinician = TestHelper.createUser();
        context.getContext().setUser(author); // to propagate to acts
        context.getContext().setClinician(clinician);

        CustomerChargeActItemEditor editor = new DefaultCustomerChargeActItemEditor(item, charge, context);
        editor.getComponent();
        assertFalse(editor.isValid());

        // register a handler for act popups
        ChargeEditorQueue mgr = new ChargeEditorQueue();
        editor.setEditorQueue(mgr);

        // populate quantity, patient, product
        editor.setQuantity(quantity);
        if (!TypeHelper.isA(item, CustomerAccountArchetypes.COUNTER_ITEM)) {
            // counter sale items have no patient
            editor.setPatient(patient);
        }
        editor.setProduct(product);

        // editor should now be valid, but won't save
        assertTrue(editor.isValid());

        try {
            save(charge, editor);
            fail("Expected save to fail");
        } catch (IllegalStateException expected) {
            assertEquals("Cannot save with product template: " + product.getName(), expected.getMessage());
        }

        checkItem(item, patient, product, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        ActBean itemBean = new ActBean(item);
        // verify there are no medication acts
        assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Tests charging a product with a 10% discount.
     *
     * @param charge the charge
     * @param item   the charge item
     */
    private void checkDiscounts(FinancialAct charge, FinancialAct item) {
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        User clinician = TestHelper.createClinician();
        User author = TestHelper.createUser();
        Entity discount = DiscountTestHelper.createDiscount(BigDecimal.TEN, true, DiscountRules.PERCENTAGE);
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedCost, fixedPrice, unitCost, unitPrice);
        Party patient = TestHelper.createPatient();
        addDiscount(customer, discount);
        addDiscount(product, discount);

        context.setUser(author);
        context.setClinician(clinician);
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));

        // create the editor
        TestCustomerChargeActItemEditor editor = new TestCustomerChargeActItemEditor(item, charge, layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        if (!TypeHelper.isA(item, CustomerAccountArchetypes.COUNTER_ITEM)) {
            // counter sale items have no patient
            editor.setPatient(patient);
        }
        editor.setProduct(product);
        editor.setQuantity(quantity);

        // editor should now be valid
        assertTrue(editor.isValid());

        checkSave(charge, editor);

        item = get(item);
        BigDecimal discount1 = new BigDecimal("2.20");
        BigDecimal tax1 = new BigDecimal("1.80");
        BigDecimal total1 = new BigDecimal("19.80");
        checkItem(item, patient, product, author, clinician, quantity, unitCost, unitPrice, fixedCost,
                  fixedPrice, discount1, tax1, total1);

        // now remove the discounts
        editor.setDiscount(BigDecimal.ZERO);
        checkSave(charge, editor);

        item = get(item);
        BigDecimal discount2 = BigDecimal.ZERO;
        BigDecimal tax2 = new BigDecimal("2.00");
        BigDecimal total2 = new BigDecimal("22.00");
        checkItem(item, patient, product, author, clinician, quantity, unitCost, unitPrice, fixedCost,
                  fixedPrice, discount2, tax2, total2);
    }

    /**
     * Saves a charge and charge item editor in a single transaction, verifying the save was successful.
     *
     * @param charge the charge
     * @param editor the charge item editor
     */
    private void checkSave(final FinancialAct charge, final CustomerChargeActItemEditor editor) {
        boolean result = save(charge, editor);
        assertTrue(result);
    }

    /**
     * Saves a charge and charge item editor in a single transaction.
     *
     * @param charge the charge
     * @param editor the charge item editor
     * @return <tt>true</tt> if the save was successful, otherwise <tt>false</tt>
     */
    private boolean save(final FinancialAct charge, final CustomerChargeActItemEditor editor) {
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        return template.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                PatientHistoryChanges changes = new PatientHistoryChanges(null, null, getArchetypeService());
                ChargeContext context = new ChargeContext();
                context.setHistoryChanges(changes);
                editor.setChargeContext(context);
                boolean saved = SaveHelper.save(charge) && editor.save();
                if (saved) {
                    context.save();
                }
                context.setHistoryChanges(null);
                return saved;
            }
        });
    }

    private static class TestCustomerChargeActItemEditor extends CustomerChargeActItemEditor {

        /**
         * Constructs a {@link TestCustomerChargeActItemEditor}.
         * <p/>
         * This recalculates the tax amount.
         *
         * @param act     the act to edit
         * @param parent  the parent act
         * @param context the layout context
         */
        public TestCustomerChargeActItemEditor(Act act, Act parent, LayoutContext context) {
            super(act, parent, context);
        }

        @Override
        public ActRelationshipCollectionEditor getDispensingEditor() {
            return super.getDispensingEditor();
        }
    }
}
