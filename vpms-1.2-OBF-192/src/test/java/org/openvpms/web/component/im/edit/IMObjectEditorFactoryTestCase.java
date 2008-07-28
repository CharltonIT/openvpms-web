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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.app.admin.lookup.LookupEditor;
import org.openvpms.web.app.admin.template.DocumentTemplatePrinterEditor;
import org.openvpms.web.app.customer.CustomerEditor;
import org.openvpms.web.app.customer.PatientOwnerRelationshipEditor;
import org.openvpms.web.app.customer.account.AdjustmentActEditor;
import org.openvpms.web.app.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.app.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.app.patient.PatientEditor;
import org.openvpms.web.app.patient.mr.PatientClinicalEventActEditor;
import org.openvpms.web.app.patient.mr.PatientClinicalProblemActEditor;
import org.openvpms.web.app.product.ProductPriceEditor;
import org.openvpms.web.app.product.stock.StockAdjustEditor;
import org.openvpms.web.app.product.stock.StockAdjustItemEditor;
import org.openvpms.web.app.product.stock.StockTransferEditor;
import org.openvpms.web.app.product.stock.StockTransferItemEditor;
import org.openvpms.web.app.supplier.charge.SupplierChargeActEditor;
import org.openvpms.web.app.supplier.charge.SupplierChargeActItemEditor;
import org.openvpms.web.app.supplier.delivery.DeliveryEditor;
import org.openvpms.web.app.supplier.delivery.DeliveryItemEditor;
import org.openvpms.web.app.supplier.order.OrderEditor;
import org.openvpms.web.app.supplier.order.OrderItemEditor;
import org.openvpms.web.app.workflow.messaging.UserMessageActEditor;
import org.openvpms.web.component.im.doc.DocumentTemplateEditor;
import org.openvpms.web.component.im.edit.act.DefaultParticipationEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.edit.estimation.EstimationEditor;
import org.openvpms.web.component.im.edit.estimation.EstimationItemEditor;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.im.edit.payment.PaymentItemEditor;
import org.openvpms.web.component.im.edit.payment.SupplierPaymentEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.ProductEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.relationship.EntityRelationshipEditor;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;


/**
 * {@link IMObjectEditorFactory} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectEditorFactoryTestCase extends AbstractAppTest {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Verifies that a {@link DefaultIMObjectEditor} is returned when no other
     * class is configured.
     */
    public void testCreateDefaultEditor() {
        checkCreate("contact.location", DefaultIMObjectEditor.class);
    }

    /**
     * Verifies that an {@link EntityRelationshipEditor} is returned for
     * all <em>entityRelationship.*</em> short names, with the exception
     * of <em>entityRelationship.patientOwner</em> which should return
     * an {@link PatientOwnerRelationshipEditor} and
     * <em>entityRelationship.documentTemplatePrinter</em>
     * which should return an {@link DocumentTemplatePrinterEditor}.
     */
    public void testCreateRelationshipEditor() {
        String[] shortNames
                = DescriptorHelper.getShortNames("entityRelationship.*");
        for (String shortName : shortNames) {
            if (shortName.equals("entityRelationship.patientOwner")) {
                checkCreate(shortName, PatientOwnerRelationshipEditor.class);
            } else if (shortName.equals(
                    "entityRelationship.documentTemplatePrinter")) {
                checkCreate(shortName, DocumentTemplatePrinterEditor.class);
            } else {
                checkCreate(shortName, EntityRelationshipEditor.class);
            }
        }
    }

    /**
     * Verifies that a {@link DefaultParticipationEditor} is returned for
     * participation.author</em>
     */
    public void testCreateDefaultParticipationEditor() {
        String[] shortNames = {"participation.author"};
        for (String shortName : shortNames) {
            checkCreate(shortName, "act.customerEstimationItem",
                        DefaultParticipationEditor.class);
        }
    }

    /**
     * Verifies that a {@link PatientParticipationEditor} is returned for
     * <em>participation.patient</em>
     */
    public void testCreatePatientParticipationEditor() {
        checkCreate("participation.patient", "act.customerEstimationItem",
                    PatientParticipationEditor.class);
    }

    /**
     * Verifies that a {@link ProductParticipationEditor} is returned for
     * <em>participation.product</em>
     */
    public void testCreateProductParticipationEditor() {
        checkCreate("participation.product", "act.customerEstimationItem",
                    ProductParticipationEditor.class);
    }

    /**
     * Verifies that a {@link EstimationEditor} is returned for
     * <em>act.customerEstimationEditor</em>.
     */
    public void testCreateEstimationEditor() {
        checkCreate("act.customerEstimation", EstimationEditor.class);
    }

    /**
     * Verfies that a {@link EstimationItemEditor} is returned for
     * <em>act.customerEstimationItem</em>
     */
    public void testCreateEstimationItemEditor() {
        checkCreate("act.customerEstimationItem", "act.customerEstimation",
                    EstimationItemEditor.class);
    }

    /**
     * Verifies that a {@link CustomerChargeActEditor} is returned for
     * <em>act.customerAccountChargesInvoice, act.customerAccountChargesCredit,
     * act.customerAccountChargesCounter, act.supplierAccountChargesInvoice, and
     * act.supplierAccountChargesCredit</em>
     */
    public void testCreateChargeEditor() {
        checkCreate("act.customerAccountChargesInvoice",
                    CustomerChargeActEditor.class);
        checkCreate("act.customerAccountChargesCredit",
                    CustomerChargeActEditor.class);
        checkCreate("act.customerAccountChargesCounter",
                    CustomerChargeActEditor.class);
    }

    /**
     * Verfies that a {@link CustomerChargeActItemEditor} is created for
     * <em>act.customerAccountInvoiceItem, act.customerAccountCreditItem and
     * act.customerAccountCounterItem</em>
     */
    public void testCreateCustomerIvoiceItemEditor() {
        checkCreate("act.customerAccountInvoiceItem",
                    "act.customerAccountChargesInvoice",
                    CustomerChargeActItemEditor.class);
        checkCreate("act.customerAccountCreditItem",
                    "act.customerAccountChargesCredit",
                    CustomerChargeActItemEditor.class);
        checkCreate("act.customerAccountCounterItem",
                    "act.customerAccountChargesCounter",
                    CustomerChargeActItemEditor.class);
    }

    /**
     * Verifies that a {@link SupplierChargeActEditor} is returned for
     * <em>act.customerAccountChargesInvoice, act.customerAccountChargesCredit,
     * act.customerAccountChargesCounter, act.supplierAccountChargesInvoice, and
     * act.supplierAccountChargesCredit</em>
     */
    public void testCreateSupplierInvoiceEditor() {
        checkCreate("act.supplierAccountChargesInvoice",
                    SupplierChargeActEditor.class);
        checkCreate("act.supplierAccountChargesCredit",
                    SupplierChargeActEditor.class);
    }

    /**
     * Verfies that a {@link CustomerPaymentEditor} is created for
     * <em>act.customerAccountPayment and act.customerAccountRefund.
     */
    public void testCreatePaymentEditor() {
        checkCreate("act.customerAccountPayment", CustomerPaymentEditor.class);
        checkCreate("act.customerAccountRefund", CustomerPaymentEditor.class);
    }

    /**
     * Verfies that a {@link PaymentItemEditor} is created for
     * <em>act.customerAccountPayment.*<em>,
     * <em>act.customerAccountRefund.*</em>,
     * <em>act.supplierAccountPayment.*</em> and
     * <em>act.supplierAccountRefund.*</em>.
     */
    public void testCreatePaymentItemEditor() {
        checkCreate("act.customerAccountPaymentCash",
                    PaymentItemEditor.class);
        checkCreate("act.customerAccountPaymentCheque",
                    PaymentItemEditor.class);
        checkCreate("act.customerAccountPaymentCredit",
                    PaymentItemEditor.class);
        checkCreate("act.customerAccountPaymentEFT",
                    PaymentItemEditor.class);
        checkCreate("act.customerAccountRefundCash",
                    PaymentItemEditor.class);
        checkCreate("act.customerAccountRefundCredit",
                    PaymentItemEditor.class);
        checkCreate("act.customerAccountRefundEFT",
                    PaymentItemEditor.class);
        checkCreate("act.supplierAccountPaymentCash",
                    PaymentItemEditor.class);
        checkCreate("act.supplierAccountPaymentCheque",
                    PaymentItemEditor.class);
        checkCreate("act.supplierAccountPaymentCredit",
                    PaymentItemEditor.class);
        checkCreate("act.supplierAccountPaymentEFT",
                    PaymentItemEditor.class);
        checkCreate("act.supplierAccountRefundCash",
                    PaymentItemEditor.class);
        checkCreate("act.supplierAccountRefundCheque",
                    PaymentItemEditor.class);
        checkCreate("act.supplierAccountRefundCredit",
                    PaymentItemEditor.class);
        checkCreate("act.supplierAccountRefundEFT",
                    PaymentItemEditor.class);
    }

    /**
     * Verfies that a {@link SupplierPaymentEditor} is created for
     * act.supplierAccountPayment and act.supplierAccountRefund</em>
     */
    public void testCreateSupplierPaymentEditor() {
        checkCreate("act.supplierAccountPayment", SupplierPaymentEditor.class);
        checkCreate("act.supplierAccountRefund", SupplierPaymentEditor.class);
    }

    /**
     * Verifies that an {@link ProductEditor} is created for each
     * <em>product.*</em> archetype.
     */
    public void testCreateProductEditor() {
        for (String shortName : DescriptorHelper.getShortNames("product.*")) {
            checkCreate(shortName, ProductEditor.class);
        }
    }

    /**
     * Verifies that an {@link OrderEditor} is created for
     * <em>act.supplierOrder</em>
     */
    public void testCreateOrderEditor() {
        checkCreate("act.supplierOrder", OrderEditor.class);
    }

    /**
     * Verifies that an {@link OrderItemEditor} is created for
     * <em>act.supplierOrderItem</em>
     */
    public void testCreateOrderItemEditor() {
        checkCreate("act.supplierOrderItem", OrderItemEditor.class);
    }

    /**
     * Verifies that an {@link DeliveryEditor} is created for
     * <em>act.supplierDelivery</em> and <em>act.supplierReturn</em>.
     */
    public void testCreateDeliveryEditor() {
        checkCreate("act.supplierDelivery", DeliveryEditor.class);
        checkCreate("act.supplierReturn", DeliveryEditor.class);
    }

    /**
     * Verifies that an {@link DeliveryItemEditor} is created for
     * <em>act.supplierDeliveryItem</em> and <em>act.supplierReturnItem</em>.
     */
    public void testCreateDeliveryItemEditor() {
        checkCreate("act.supplierDeliveryItem", DeliveryItemEditor.class);
        checkCreate("act.supplierReturnItem", DeliveryItemEditor.class);
    }

    /**
     * Verifies that a {@link SupplierChargeActItemEditor} is created for
     * <em>act.supplierAccountInvoiceItem and act.supplierAccountCreditItem</em>
     */
    public void testCreateSupplierInvoiceItemEditor() {
        checkCreate("act.supplierAccountInvoiceItem",
                    SupplierChargeActItemEditor.class);
        checkCreate("act.supplierAccountCreditItem",
                    SupplierChargeActItemEditor.class);
    }

    /**
     * Verifies that a {@link StockAdjustEditor} is created for
     * <em>act.stockAdjust</em>.
     */
    public void testCreateStockAdjustEditor() {
        checkCreate("act.stockAdjust", StockAdjustEditor.class);
    }

    /**
     * Verifies that a {@link StockAdjustItemEditor} is created for
     * <em>act.stockAdjustItem</em>.
     */
    public void testCreateStockAdjustItemEditor() {
        checkCreate("act.stockAdjustItem", StockAdjustItemEditor.class);
    }

    /**
     * Verifies that a {@link StockTransferEditor} is created for
     * <em>act.stockTransfer</em>.
     */
    public void testCreateStockTransferEditor() {
        checkCreate("act.stockTransfer", StockTransferEditor.class);
    }

    /**
     * Verifies that a {@link StockTransferItemEditor} is created for
     * <em>act.stockTransferItem</em>.
     */
    public void testCreateStockTransferItemEditor() {
        checkCreate("act.stockTransferItem", StockTransferItemEditor.class);
    }

    /**
     * Verifies that an {@link AdjustmentActEditor} is created for
     * <em>act.customerAccountBadDebt</em>,
     * <em>act.customerAccountDebitAdjust</em> and
     * <em>act.customerAccountCreditAdjust</em>
     */
    public void testCreateAdjustmentActEditor() {
        checkCreate("act.customerAccountBadDebt", AdjustmentActEditor.class);
        checkCreate("act.customerAccountCreditAdjust",
                    AdjustmentActEditor.class);
        checkCreate("act.customerAccountDebitAdjust",
                    AdjustmentActEditor.class);
    }

    /**
     * Verifies that an {@link UserMessageActEditor} is created for
     * <em>act.userMessage</em>
     */
    public void testCreateUserMessageActEditor() {
        checkCreate("act.userMessage", UserMessageActEditor.class);
    }

    /**
     * Verifies that an {@link DocumentTemplateEditor} is created for
     * <em>entity.documentTemplate</em>
     */
    public void testCreateDocumentTemplateEditor() {
        checkCreate("entity.documentTemplate", DocumentTemplateEditor.class);
    }

    /**
     * Verifies that an {@link CustomerEditor} is created for
     * <em>party.customer*</em>
     */
    public void testCreateCustomerEditor() {
        String[] shortNames
                = DescriptorHelper.getShortNames("party.customer*");
        for (String shortName : shortNames) {
            checkCreate(shortName, CustomerEditor.class);
        }
    }

    /**
     * Verifies that an {@link PatientEditor} is created for
     * <em>party.patientpet</em>
     */
    public void testCreatePatientEditor() {
        checkCreate("party.patientpet", PatientEditor.class);
    }

    /**
     * Verifies that a {@link LookupEditor} is created for <em>lookup.*</em>
     */
    public void testCreateLookupEditor() {
        checkCreate("lookup.alertType", LookupEditor.class);
    }

    /**
     * Verifies that a {@link PatientClinicalEventActEditor} is created
     * for <em>act.patientClinicalEvent</em>.
     */
    public void testCreatePatientClinicalEventActEditor() {
        checkCreate("act.patientClinicalEvent",
                    PatientClinicalEventActEditor.class);
    }

    /**
     * Verifies that a {@link PatientClinicalProblemActEditor} is created
     * for <em>act.patientClinicalProblem</em>.
     */
    public void testCreatePatientClinicalProblemActEditor() {
        checkCreate("act.patientClinicalProblem",
                    PatientClinicalProblemActEditor.class);
    }

    /**
     * Verifies that a {@link ProductPriceEditor} is created for
     * <em>productPrice.fixedPrice</em> and <em>productPrice.unitPrice</em>.
     */
    public void testCreateProductPriceEditor() {
        checkCreate("productPrice.fixedPrice", ProductPriceEditor.class);
        checkCreate("productPrice.unitPrice", ProductPriceEditor.class);
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
     * Verifies that the editor returned by {@link IMObjectEditorFactory#create}
     * matches that expected.
     *
     * @param shortName name the archetype short name
     * @param type      the expected editor class
     */
    private void checkCreate(String shortName, Class type) {
        LayoutContext context = new DefaultLayoutContext();
        IMObject object = service.create(shortName);
        assertNotNull("Failed to create object with shortname=" + shortName,
                      object);
        IMObjectEditor editor = IMObjectEditorFactory.create(object, context);
        assertNotNull("Failed to create editor", editor);
        assertEquals(type, editor.getClass());
    }

    /**
     * Verifies that the editor returned by {@link IMObjectEditorFactory#create}
     * matches that expected.
     *
     * @param shortName       name the archetype short name
     * @param parentShortName the parent archetype short name
     * @param type            the expected editor class
     */
    private void checkCreate(String shortName, String parentShortName,
                             Class type) {
        LayoutContext context = new DefaultLayoutContext();
        IMObject object = service.create(shortName);
        assertNotNull("Failed to create object with shortname=" + shortName,
                      object);
        IMObject parent = service.create(parentShortName);
        assertNotNull("Failed to create object with shortname="
                + parentShortName, parent);
        IMObjectEditor editor = IMObjectEditorFactory.create(object, parent,
                                                             context);
        assertNotNull("Failed to create editor", editor);
        assertEquals(type, editor.getClass());
    }

}