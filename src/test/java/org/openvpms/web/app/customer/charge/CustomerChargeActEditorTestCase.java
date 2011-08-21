package org.openvpms.web.app.customer.charge;

import org.apache.commons.lang.ObjectUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tests the {@link CustomerChargeActEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerChargeActEditorTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * Tests creation and saving of an empty invoice.
     */
    @Test
    public void testEmptyInvoice() {
        checkEmptyCharge((FinancialAct) create(CustomerAccountArchetypes.INVOICE));
    }

    /**
     * Tests creation and saving of an empty invoice.
     */
    @Test
    public void testEmptyCredit() {
        checkEmptyCharge((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Tests creation and saving of an empty counter sale.
     */
    @Test
    public void testEmptyCounterSale() {
        checkEmptyCharge((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Tests invoicing.
     */
    @Test
    public void testInvoice() {
        checkEditCharge((FinancialAct) create(CustomerAccountArchetypes.INVOICE));
    }

    /**
     * Tests counter sales.
     */
    @Test
    public void testCounterSale() {
        checkEditCharge((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Tests credits.
     */
    @Test
    public void testCredit() {
        checkEditCharge((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes an invoice and its item.
     */
    @Test
    public void testDeleteInvoice() {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new Money(100), customer, patient, product,
                                                                           ActStatus.IN_PROGRESS);
        checkDeleteCharge(acts.get(0), acts.get(1));
    }

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes a credit and its item.
     */
    @Test
    public void testDeleteCredit() {
        Party customer = TestHelper.createCustomer();
        Party patient = TestHelper.createPatient();
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new Money(100), customer, patient, product,
                                                                          ActStatus.IN_PROGRESS);
        checkDeleteCharge(acts.get(0), acts.get(1));
    }

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes a counter sale and its item.
     */
    @Test
    public void testDeleteCounterSale() {
        Party customer = TestHelper.createCustomer();
        Product product = TestHelper.createProduct();
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new Money(100), customer, product,
                                                                           ActStatus.IN_PROGRESS);
        checkDeleteCharge(acts.get(0), acts.get(1));
    }

    /**
     * Verifies stock quantities update for products used in an invoice.
     */
    @Test
    public void testInvoiceStockUpdate() {
        checkChargeStockUpdate((FinancialAct) create(CustomerAccountArchetypes.INVOICE));
    }

    /**
     * Verifies stock quantities update for products used in a credit.
     */
    @Test
    public void testCreditStockUpdate() {
        checkChargeStockUpdate((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Verifies stock quantities update for products used in a counter sale.
     */
    @Test
    public void testCounterSaleStockUpdate() {
        checkChargeStockUpdate((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes an invoice and its item.
     *
     * @param charge the charge
     * @param item   the charge item
     */
    private void checkDeleteCharge(FinancialAct charge, FinancialAct item) {
        save(charge, item);
        final CustomerChargeActEditor editor = new CustomerChargeActEditor(charge, null, new DefaultLayoutContext());
        editor.getComponent();
        Boolean result = delete(editor);
        assertTrue(result);

        assertNull(get(charge));
        assertNull(get(item));
    }

    /**
     * Tests editing a charge with no items.
     *
     * @param charge the charge
     */
    private void checkEmptyCharge(FinancialAct charge) {
        LayoutContext context = new DefaultLayoutContext();
        Party customer = TestHelper.createCustomer();
        Party location = TestHelper.createLocation();
        User author = TestHelper.createUser();
        context.getContext().setCustomer(customer);
        context.getContext().setLocation(location);
        context.getContext().setUser(author);

        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, context, new EditorManager());
        editor.getComponent();
        assertTrue(editor.isValid());
        assertTrue(editor.save());
        checkBalance(customer, BigDecimal.ZERO, BigDecimal.ZERO);

        editor.setStatus(ActStatus.POSTED);
        assertTrue(editor.save());
        checkBalance(customer, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Tests editing of a charge.
     *
     * @param charge the charge
     */
    private void checkEditCharge(FinancialAct charge) {
        LayoutContext context = new DefaultLayoutContext();
        Party customer = TestHelper.createCustomer();
        Party location = TestHelper.createLocation(true);   // enable stock control
        Party stockLocation = createStockLocation(location);
        Party patient = TestHelper.createPatient(customer);
        User author = TestHelper.createUser();
        User clinician = TestHelper.createClinician();
        context.getContext().setCustomer(customer);
        context.getContext().setLocation(location);
        context.getContext().setStockLocation(stockLocation);
        context.getContext().setUser(author);
        context.getContext().setClinician(clinician);

        BigDecimal fixedPrice = BigDecimal.valueOf(11);
        BigDecimal itemTax = BigDecimal.valueOf(1);
        BigDecimal itemTotal = BigDecimal.valueOf(11);
        BigDecimal tax = itemTax.multiply(BigDecimal.valueOf(3));
        BigDecimal total = itemTotal.multiply(BigDecimal.valueOf(3));

        boolean invoice = TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE);

        Product product1 = createProduct(ProductArchetypes.MEDICATION, fixedPrice);
        addReminder(product1);
        addInvestigation(product1);
        addTemplate(product1);
        int product1Acts = invoice ? 4 : 0;

        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, fixedPrice);
        addReminder(product2);
        addInvestigation(product2);
        addTemplate(product2);
        int product2Acts = invoice ? 3 : 0;

        Product product3 = createProduct(ProductArchetypes.SERVICE, fixedPrice);
        addReminder(product3);
        addInvestigation(product3);
        addTemplate(product3);
        int product3Acts = invoice ? 3 : 0;

        BigDecimal product1Stock = BigDecimal.valueOf(100);
        BigDecimal product2Stock = BigDecimal.valueOf(50);
        initStock(product1, stockLocation, product1Stock);
        initStock(product2, stockLocation, product2Stock);

        EditorManager mgr = new EditorManager();
        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, context, mgr);
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = BigDecimal.ONE;
        addItem(editor, patient, product1, quantity, mgr);
        addItem(editor, patient, product2, quantity, mgr);
        addItem(editor, patient, product3, quantity, mgr);

        assertTrue(editor.isValid());
        assertTrue(SaveHelper.save(editor));
        BigDecimal balance = (charge.isCredit()) ? total.negate() : total;
        checkBalance(customer, balance, BigDecimal.ZERO);
        editor.setStatus(ActStatus.POSTED);
        assertTrue(editor.save());
        checkBalance(customer, BigDecimal.ZERO, balance);

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());
        checkCharge(charge, customer, author, clinician, tax, total);
        BigDecimal discount = BigDecimal.ZERO;
        checkItem(items, patient, product1, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, fixedPrice, discount, itemTax, itemTotal, product1Acts);
        checkItem(items, patient, product2, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, fixedPrice, discount, itemTax, itemTotal, product2Acts);
        checkItem(items, patient, product3, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, fixedPrice, discount, itemTax, itemTotal, product3Acts);

        boolean add = bean.isA(CustomerAccountArchetypes.CREDIT);
        checkStock(product1, stockLocation, product1Stock, quantity, add);
        checkStock(product2, stockLocation, product2Stock, quantity, add);
        checkStock(product3, stockLocation, BigDecimal.ZERO, BigDecimal.ZERO, add);
    }

    /**
     * Verifies stock quantities update for products used in a charge.
     *
     * @param charge the charge
     */
    private void checkChargeStockUpdate(FinancialAct charge) {
        LayoutContext context = new DefaultLayoutContext();
        Party customer = TestHelper.createCustomer();
        Party location = TestHelper.createLocation(true);   // enable stock control
        Party stockLocation = createStockLocation(location);
        Party patient = TestHelper.createPatient(customer);
        User author = TestHelper.createUser();
        User clinician = TestHelper.createClinician();
        context.getContext().setCustomer(customer);
        context.getContext().setLocation(location);
        context.getContext().setStockLocation(stockLocation);
        context.getContext().setUser(author);
        context.getContext().setClinician(clinician);

        Product product1 = createProduct(ProductArchetypes.MEDICATION);
        Product product2 = createProduct(ProductArchetypes.MERCHANDISE);
        Product product3 = createProduct(ProductArchetypes.SERVICE);

        BigDecimal product1InitialStock = BigDecimal.valueOf(100);
        BigDecimal product2InitialStock = BigDecimal.valueOf(50);
        initStock(product1, stockLocation, product1InitialStock);
        initStock(product2, stockLocation, product2InitialStock);

        EditorManager mgr = new EditorManager();
        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, context, mgr);
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity1 = BigDecimal.valueOf(5);
        BigDecimal quantity2 = BigDecimal.valueOf(10);

        CustomerChargeActItemEditor item1 = addItem(editor, patient, product1, quantity1, mgr);
        CustomerChargeActItemEditor item2 = addItem(editor, patient, product2, quantity2, mgr);
        assertTrue(SaveHelper.save(editor));

        boolean add = TypeHelper.isA(charge, CustomerAccountArchetypes.CREDIT);
        BigDecimal product1Stock = checkStock(product1, stockLocation, product1InitialStock, quantity1, add);
        BigDecimal product2Stock = checkStock(product2, stockLocation, product2InitialStock, quantity2, add);

        item1.setQuantity(BigDecimal.valueOf(10)); // change product1 stock quantity
        item2.setProduct(product3);                // change the product and verify the stock for product2 reverts
        assertTrue(SaveHelper.save(editor));
        checkStock(product1, stockLocation, product1Stock, BigDecimal.valueOf(5), add);
        checkStock(product2, stockLocation, product2Stock, quantity2, !add);

        // now delete the charge and verify the stock reverts
        assertTrue(delete(editor));
        checkStock(product1, stockLocation, product1InitialStock);
        checkStock(product2, stockLocation, product2InitialStock);
    }

    /**
     * Initialises stock quantities for a product at a stock location.
     *
     * @param product the product
     * @param stockLocation the stock location
     * @param quantity the initial stock quantity
     */
    private void initStock(Product product, Party stockLocation, BigDecimal quantity) {
        StockRules rules = new StockRules();
        rules.updateStock(product, stockLocation, quantity);
    }

    /**
     * Checks stock for a product at a stock location.
     *
     * @param product the product
     * @param stockLocation the stock location
     * @param initial the initial stock quantity
     * @param change the change in stock quantity
     * @param add if <tt>true</tt> add the change, otherwise subtract it
     * @return the new stock quantity
     */
    private BigDecimal checkStock(Product product, Party stockLocation, BigDecimal initial, BigDecimal change,
                                  boolean add) {
        BigDecimal expected = add ? initial.add(change) : initial.subtract(change);
        checkStock(product, stockLocation, expected);
        return expected;
    }

    /**
     * Checks stock for a product at a stock location.
     *
     * @param product the product
     * @param stockLocation the stock location
     * @param expected the expected stock quantity
     */
    private void checkStock(Product product, Party stockLocation, BigDecimal expected) {
        StockRules rules = new StockRules();
        checkEquals(expected, rules.getStock(get(product), get(stockLocation)));
    }

    /**
     * Verifies a charge matches that expected.
     *
     * @param charge    the charge
     * @param customer  the expected customer
     * @param author    the expected author
     * @param clinician the expected clinician
     * @param tax       the expected tax
     * @param total     the expected total
     */
    private void checkCharge(FinancialAct charge, Party customer, User author, User clinician, BigDecimal tax,
                             BigDecimal total) {
        ActBean bean = new ActBean(charge);
        assertEquals(customer.getObjectReference(), bean.getNodeParticipantRef("customer"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        if (bean.hasNode("clinician")) {
            assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        }
        checkEquals(tax, bean.getBigDecimal("tax"));
        checkEquals(total, bean.getBigDecimal("amount"));
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param items      the items to search
     * @param patient    the expected patient
     * @param product    the expected product
     * @param author     the expected author
     * @param clinician  the expected clinician
     * @param quantity   the expected quantity
     * @param unitCost   the expected unit cost
     * @param unitPrice  the expected unit price
     * @param fixedCost  the expected fixed cost
     * @param fixedPrice the expected fixed price
     * @param discount   the expected discount
     * @param tax        the expected tax
     * @param total      the expected total
     * @param childActs  the expected no. of child acts
     */
    private void checkItem(List<FinancialAct> items, Party patient, Product product, User author, User clinician,
                           BigDecimal quantity, BigDecimal unitCost, BigDecimal unitPrice, BigDecimal fixedCost,
                           BigDecimal fixedPrice, BigDecimal discount, BigDecimal tax, BigDecimal total,
                           int childActs) {
        int count = 0;
        FinancialAct item = find(items, product);
        checkItem(item, patient, product, author, clinician, quantity, unitCost, unitPrice, fixedCost, fixedPrice,
                  discount, tax, total);
        ActBean itemBean = new ActBean(item);
        EntityBean bean = new EntityBean(product);
        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
                // verify there is a medication act
                checkMedication(item, patient, product, author, clinician);
                ++count;
            } else {
                assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
            }
            for (Entity investigationType : bean.getNodeTargetEntities("investigationTypes")) {
                checkInvestigation(item, patient, investigationType, author, clinician);
                ++count;
            }
            for (Entity reminderType : bean.getNodeTargetEntities("reminders")) {
                checkReminder(item, patient, product, reminderType, author, clinician);
                ++count;
            }
            for (Entity template : bean.getNodeTargetEntities("documents")) {
                checkDocument(item, patient, product, template, author, clinician);
                ++count;
            }
        } else {
            // verify there are no medication, investigation, reminder nor document acts
            assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
            assertTrue(itemBean.getActs(InvestigationArchetypes.PATIENT_INVESTIGATION).isEmpty());
            assertTrue(itemBean.getActs(ReminderArchetypes.REMINDER).isEmpty());
            assertTrue(itemBean.getActs("act.patientDocument*").isEmpty());
        }
        assertEquals(childActs, count);
    }

    /**
     * Deletes a charge.
     *
     * @param editor the editor to use
     * @return <tt>true</tt> if the delete was successful
     */
    private boolean delete(final CustomerChargeActEditor editor) {
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        return template.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                return editor.delete();
            }
        });
    }

    /**
     * Finds a charge item in a list of items, by product.
     *
     * @param items   the items
     * @param product the product
     * @return the corresponding item
     */
    private FinancialAct find(List<FinancialAct> items, Product product) {
        FinancialAct result = null;
        for (FinancialAct item : items) {
            ActBean current = new ActBean(item);
            if (ObjectUtils.equals(current.getNodeParticipantRef("product"), product.getObjectReference())) {
                result = item;
                break;
            }
        }
        assertNotNull(result);
        return result;
    }

    /**
     * Adds a charge item.
     *
     * @param editor   the editor
     * @param patient  the patient
     * @param product  the product
     * @param quantity the quantity
     * @param mgr      the popup editor manager
     * @return the editor for the new item
     */
    private CustomerChargeActItemEditor addItem(CustomerChargeActEditor editor, Party patient, Product product,
                                                BigDecimal quantity, EditorManager mgr) {
        CustomerChargeActItemEditor itemEditor = editor.addItem();
        itemEditor.getComponent();
        assertTrue(editor.isValid());
        assertFalse(itemEditor.isValid());

        if (itemEditor.getProperty("patient") != null) {
            itemEditor.setPatient(patient);
        }
        itemEditor.setProduct(product);
        itemEditor.setQuantity(quantity);
        if (TypeHelper.isA(editor.getObject(), CustomerAccountArchetypes.INVOICE)) {
            if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
                // invoice items have a dispensing node
                assertFalse(itemEditor.isValid());  // not valid while popup is displayed
                checkSavePopup(mgr, PatientArchetypes.PATIENT_MEDICATION);
                // save the popup editor - should be a medication
            }

            EntityBean bean = new EntityBean(product);
            for (int i = 0; i < bean.getNodeTargetEntityRefs("investigationTypes").size(); ++i) {
                assertFalse(editor.isValid()); // not valid while popup is displayed
                checkSavePopup(mgr, InvestigationArchetypes.PATIENT_INVESTIGATION);
            }
            for (int i = 0; i < bean.getNodeTargetEntityRefs("reminders").size(); ++i) {
                assertFalse(editor.isValid()); // not valid while popup is displayed
                checkSavePopup(mgr, ReminderArchetypes.REMINDER);
            }
        }
        assertTrue(itemEditor.isValid());
        return itemEditor;
    }

    /**
     * Checks the balance for a customer.
     *
     * @param customer the customer
     * @param unbilled the expected unbilled amount
     * @param balance  the expected balance
     */
    private void checkBalance(Party customer, BigDecimal unbilled, BigDecimal balance) {
        CustomerAccountRules rules = new CustomerAccountRules();
        checkEquals(unbilled, rules.getUnbilledAmount(customer));
        checkEquals(balance, rules.getBalance(customer));
    }

    /**
     * Creates a customer charge act editor.
     *
     * @param invoice the charge to edit
     * @param context the layout context
     * @param manager the popup editor manager
     * @return a new customer charge act editor
     */
    private CustomerChargeActEditor createCustomerChargeActEditor(final FinancialAct invoice,
                                                                  final LayoutContext context,
                                                                  final PopupEditorManager manager) {
        return new CustomerChargeActEditor(invoice, null, context, false) {
            @Override
            protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
                ActRelationshipCollectionEditor editor = super.createItemsEditor(act, items);
                if (editor instanceof ChargeItemRelationshipCollectionEditor) {
                    // register a handler for act popups
                    ((ChargeItemRelationshipCollectionEditor) editor).setPopupEditorManager(manager);
                }
                return editor;
            }
        };
    }

    /**
     * Helper to create a new stock location, linked to a location.
     *
     * @param location the location
     * @return the stock location
     */
    private Party createStockLocation(Party location) {
        Party stockLocation = (Party) create(StockArchetypes.STOCK_LOCATION);
        stockLocation.setName("STOCK-LOCATION-" + stockLocation.hashCode());
        EntityBean locationBean = new EntityBean(location);
        locationBean.addRelationship("entityRelationship.locationStockLocation", stockLocation);
        save(location, stockLocation);
        return stockLocation;
    }
}
