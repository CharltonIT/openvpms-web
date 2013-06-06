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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.customer.charge;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.invoice.ChargeItemEventLinker;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.FinancialActEditor;
import org.openvpms.web.component.im.edit.act.TemplateProductListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * An editor for {@link org.openvpms.component.business.domain.im.act.Act}s which have an archetype of
 * <em>act.customerAccountChargesInvoice</em>,
 * <em>act.customerAccountChargesCredit</em>
 * or <em>act.customerAccountChargesCounter</em>.
 *
 * @author Tim Anderson
 */
public class AbstractCustomerChargeActEditor extends FinancialActEditor {

    /**
     * Determines if a default item should added if no items are present.
     */
    private boolean addDefaultItem;

    /**
     * Constructs an {@code AbstractCustomerChargeActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public AbstractCustomerChargeActEditor(FinancialAct act, IMObject parent, LayoutContext context) {
        this(act, parent, context, true);
    }

    /**
     * Constructs an {@code AbstractCustomerChargeActEditor}.
     *
     * @param act            the act to edit
     * @param parent         the parent object. May be {@code null}
     * @param context        the layout context
     * @param addDefaultItem if {@code true} add a default item if the act has none
     */
    public AbstractCustomerChargeActEditor(FinancialAct act, IMObject parent,
                                           LayoutContext context, boolean addDefaultItem) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("location", context.getContext().getLocation());
        this.addDefaultItem = addDefaultItem;
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE)) {
            getItems().setTemplateProductListener(new TemplateProductListener() {
                public void expanded(Product product) {
                    templateProductExpanded(product);
                }
            });
        }
    }

    /**
     * Returns the customer associated with the charge.
     *
     * @return the customer. May be {@code null}
     */
    public Party getCustomer() {
        return (Party) getParticipant("customer");
    }

    /**
     * Returns the location associated with the charge.
     *
     * @return the location. May be {@code null}
     */
    public Party getLocation() {
        return (Party) getParticipant("location");
    }

    /**
     * Returns any unprinted documents that are flagged for immediate printing.
     *
     * @return the list of unprinted documents
     */
    public List<Act> getUnprintedDocuments() {
        return getUnprintedDocuments(Collections.<Act>emptyList());
    }

    /**
     * Returns any unprinted documents that are flagged for immediate printing.
     *
     * @param exclude a list of documents to ignore
     * @return the list of unprinted documents
     */
    public List<Act> getUnprintedDocuments(List<Act> exclude) {
        List<Act> result = new ArrayList<Act>();
        ActRelationshipCollectionEditor items = getItems();
        Set<IMObjectReference> excludeRefs = new HashSet<IMObjectReference>();
        for (Act excluded : exclude) {
            excludeRefs.add(excluded.getObjectReference());
        }
        for (Act item : items.getActs()) {
            ActBean bean = new ActBean(item);
            for (ActRelationship rel : bean.getValues("documents", ActRelationship.class)) {
                IMObjectReference target = rel.getTarget();
                if (target != null && !excludeRefs.contains(target)) {
                    Act document = (Act) getObject(target);
                    if (document != null) {
                        ActBean documentBean = new ActBean(document);
                        if (!documentBean.getBoolean("printed") && documentBean.hasNode("documentTemplate")) {
                            Entity entity = (Entity) getObject(documentBean.getNodeParticipantRef("documentTemplate"));
                            if (entity != null) {
                                DocumentTemplate template = new DocumentTemplate(entity,
                                                                                 ServiceHelper.getArchetypeService());
                                if (template.getPrintMode() == DocumentTemplate.PrintMode.IMMEDIATE) {
                                    result.add(document);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Adds a new charge item, returning its editor.
     *
     * @return the charge item editor, or {@code null} if an item couldn't be created
     */
    public CustomerChargeActItemEditor addItem() {
        ActRelationshipCollectionEditor items = getItems();
        CustomerChargeActItemEditor result = (CustomerChargeActItemEditor) items.add();
        if (result != null && items.getCurrentEditor() == result) {
            // set the default focus to that of the item editor
            getFocusGroup().setDefault(result.getFocusGroup().getDefaultFocus());
        }
        return result;
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician. May be {@code null}
     */
    public void setClinician(User clinician) {
        setParticipant("clinician", clinician);
    }

    /**
     * Save any edits.
     * <p/>
     * For invoices, this links items to their corresponding clinical events, creating events as required, and marks
     * matching reminders completed.
     *
     * @return {@code true} if the save was successful
     */
    @Override
    protected boolean doSave() {
        List<Act> reminders = getNewReminders();
        boolean saved = super.doSave();
        if (saved && TypeHelper.isA(getObject(), CustomerAccountArchetypes.INVOICE)) {
            // link the items to their corresponding clinical events
            linkToEvents();

            // mark reminders that match the new reminders completed
            if (!reminders.isEmpty()) {
                ReminderRules rules = new ReminderRules(ServiceHelper.getArchetypeService());
                rules.markMatchingRemindersCompleted(reminders);
            }
        }
        return super.doSave();
    }

    /**
     * Links the charge items to their corresponding clinical events.
     */
    protected void linkToEvents() {
        Party location = getLayoutContext().getContext().getLocation();
        ChargeItemEventLinker linker = new ChargeItemEventLinker(getAuthor(), location,
                                                                 ServiceHelper.getArchetypeService());
        List<FinancialAct> items = new ArrayList<FinancialAct>();
        for (Act act : getItems().getActs()) {
            items.add((FinancialAct) act);
        }
        linker.link(items);
    }

    /**
     * Invoked when layout has completed.
     * <p/>
     * This invokes {@link #initItems()}.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        initItems();
    }

    /**
     * Updates the amount and tax when an act item changes.
     */
    @Override
    protected void onItemsChanged() {
        super.onItemsChanged();
        calculateCosts();
    }

    /**
     * Adds a default invoice item if there are no items present and {@link #addDefaultItem} is {@code true}.
     */
    private void initItems() {
        if (addDefaultItem) {
            ActRelationshipCollectionEditor items = getItems();
            CollectionProperty property = items.getCollection();
            if (property.getValues().size() == 0) {
                // no invoice items, so add one
                addItem();
            }
        }
    }

    /**
     * Calculates the fixed and unit costs.
     */
    private void calculateCosts() {
        Property fixedCost = getProperty("fixedCost");
        BigDecimal fixed = ActHelper.sum((Act) getObject(), getItems().getCurrentActs(), "fixedCost");
        fixedCost.setValue(fixed);

        Property unitCost = getProperty("unitCost");
        BigDecimal cost = BigDecimal.ZERO;
        for (Act act : getItems().getCurrentActs()) {
            cost = cost.add(calcTotalUnitCost(act));
        }
        unitCost.setValue(cost);
    }

    /**
     * Calculates the total unit cost for an act, based on its <em>unitCost</em>
     * and <em>quantity</em>.
     *
     * @param act the act
     * @return the total unit cost
     */
    private BigDecimal calcTotalUnitCost(Act act) {
        IMObjectBean bean = new IMObjectBean(act);
        BigDecimal unitCost = bean.getBigDecimal("unitCost", BigDecimal.ZERO);
        BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
        return unitCost.multiply(quantity);
    }

    /**
     * Returns new reminders from each of the charge items.
     *
     * @return a list of new reminders
     */
    private List<Act> getNewReminders() {
        ActRelationshipCollectionEditor items = getItems();
        List<Act> reminders = new ArrayList<Act>();
        for (IMObjectEditor editor : items.getCurrentEditors()) {
            if (editor instanceof DefaultCustomerChargeActItemEditor) {
                DefaultCustomerChargeActItemEditor charge = (DefaultCustomerChargeActItemEditor) editor;
                for (Act reminder : charge.getReminders()) {
                    if (reminder.isNew()) {
                        reminders.add(reminder);
                    }
                }
            }
        }
        return reminders;
    }

    /**
     * Invoked when a template product is expanded on an invoice.
     * <p/>
     * This appends any invoiceNote to the notes.
     *
     * @param product the template product
     */
    private void templateProductExpanded(Product product) {
        Property property = getProperty("notes");
        if (property != null) {
            IMObjectBean bean = new IMObjectBean(product);
            String invoiceNote = bean.getString("invoiceNote");
            if (!StringUtils.isEmpty(invoiceNote)) {
                String value = invoiceNote;
                if (property.getValue() != null) {
                    value = property.getValue().toString();
                    if (!StringUtils.isEmpty(value)) {
                        value = value + "\n" + invoiceNote;
                    } else {
                        value = invoiceNote;
                    }
                }
                property.setValue(value);
            }
        }
    }

}