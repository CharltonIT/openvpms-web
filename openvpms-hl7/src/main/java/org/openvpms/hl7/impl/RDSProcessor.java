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

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.EI;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.group.RDS_O13_ORDER;
import ca.uhn.hl7v2.model.v25.message.RDS_O13;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.RXD;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes RDS messages.
 * <p/>
 * This generates <em>act.customerPharmacyOrder</em> messages for each RDS message received.
 * <p/>
 * Note that an RDS message may relate to a specific <em>act.customerAccountInvoiceItem</em>, and could be
 * be used to update its receivedQuantity and cancelledQuantity nodes. This is not done as it would lead to
 * version conflicts if users are editing invoices when the message is received.
 *
 * @author Tim Anderson
 */
public class RDSProcessor {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The user rules.
     */
    private final UserRules userRules;

    /**
     * Constructs a {@link RDSProcessor}.
     *
     * @param service the archetype service
     * @param rules   the patient rules
     */
    public RDSProcessor(IArchetypeService service, PatientRules rules, UserRules userRules) {
        this.service = service;
        this.rules = rules;
        this.userRules = userRules;
    }

    /**
     * Processes a dispense message.
     *
     * @param message  the message
     * @param location the practice location reference
     * @return the customer order and/or return
     * @throws HL7Exception for any HL7 error
     */
    public List<Act> process(RDS_O13 message, IMObjectReference location) throws HL7Exception {
        if (message.getORDERReps() < 1) {
            throw new HL7Exception("RDS O13 message contains no order group");
        }
        PID pid = message.getPATIENT().getPID();
        State state = getState(pid, location);
        for (int i = 0; i < message.getORDERReps(); ++i) {
            RDS_O13_ORDER group = message.getORDER(i);
            addItem(group, state);
        }
        return state.getActs();
    }

    /**
     * Adds an order item.
     *
     * @param group the order group
     * @param state the state
     */
    private void addItem(RDS_O13_ORDER group, State state) {
        BigDecimal quantity = getQuantity(group);
        ActBean bean;
        ActBean itemBean;
        if (quantity.signum() >= 0) {
            bean = state.getOrder();
            itemBean = state.createOrderItem();
        } else {
            bean = state.getReturn();
            itemBean = state.createReturnItem();
            quantity = quantity.abs();
        }

        String fillerOrderNumber = group.getORC().getFillerOrderNumber().getEntityIdentifier().getValue();
        if (fillerOrderNumber != null) {
            itemBean.setValue("reference", fillerOrderNumber);
        }
        FinancialAct invoiceItem = addInvoiceItem(group.getORC(), bean, itemBean, state);
        addClinician(group, bean, itemBean, invoiceItem);
        Product product = addProduct(group, bean, itemBean);
        if (product != null) {
            checkSellingUnits(group, bean, product);
        }
        itemBean.setValue("quantity", quantity);
    }

    /**
     * Returns the dispensed quantity.
     *
     * @param group the order group
     * @return the dispensed quantity
     */
    private BigDecimal getQuantity(RDS_O13_ORDER group) {
        String quantity = group.getRXD().getActualDispenseAmount().getValue();
        return StringUtils.isEmpty(quantity) ? BigDecimal.ZERO : new BigDecimal(quantity);
    }

    /**
     * Adds the product to the item.
     *
     * @param group    the order group
     * @param bean     the order bean
     * @param itemBean the order item bean
     * @return the product or {@code null} if none is found
     */
    private Product addProduct(RDS_O13_ORDER group, ActBean bean, ActBean itemBean) {
        RXD rxd = group.getRXD();
        CE code = rxd.getDispenseGiveCode();
        long id = getId(code);
        Product result = null;
        if (id != -1) {
            ArchetypeQuery query = new ArchetypeQuery("product.*");
            query.getArchetypeConstraint().setAlias("p");
            query.add(Constraints.eq("id", id));
            IMObjectQueryIterator<Product> iterator = new IMObjectQueryIterator<Product>(service, query);
            result = (iterator.hasNext()) ? iterator.next() : null;
        }
        if (result != null) {
            itemBean.addNodeParticipation("product", result);
        } else {
            addNote(bean, "Unknown Dispense Give Code, Id='" + code.getIdentifier().getValue()
                          + "', name='" + code.getText().getValue() + "'");
        }
        return result;
    }

    /**
     * Populates the clinician from an order group.
     * <p/>
     * If the RXD Dispensing Provider field includes a clinician, this will be used to populate the order item.
     *
     * @param group       the order group
     * @param bean        the order bean
     * @param itemBean    the order item bean
     * @param invoiceItem the original invoice item. May be {@code null}
     */
    private void addClinician(RDS_O13_ORDER group, ActBean bean, ActBean itemBean, FinancialAct invoiceItem) {
        XCN dispensingProvider = group.getRXD().getDispensingProvider(0);
        IMObjectReference clinician = null;
        long id = getId(dispensingProvider.getIDNumber().getValue());
        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(UserArchetypes.USER, id);
            User user = (User) service.get(reference);
            if (user != null && userRules.isClinician(user)) {
                clinician = user.getObjectReference();
            }
        } else if (invoiceItem != null) {
            ActBean invoiceBean = new ActBean(invoiceItem);
            // propagate the clinician from original invoice item
            clinician = invoiceBean.getNodeParticipantRef("clinician");
            if (clinician == null) {
                // else get it from the parent
                clinician = bean.getNodeParticipantRef("clinician");
            }
        }
        if (clinician != null) {
            itemBean.addNodeParticipation("clinician", clinician);
            if (bean.getNodeParticipantRef("clinician") == null) {
                bean.addNodeParticipation("clinician", clinician);
            }
        }
    }

    /**
     * Adds a reference to the original invoice item, if any.
     *
     * @param orc      the order segment
     * @param bean     the act
     * @param itemBean the item
     * @param state    the state
     */
    private FinancialAct addInvoiceItem(ORC orc, ActBean bean, ActBean itemBean, State state) {
        FinancialAct result = null;
        long id = getId(orc.getPlacerOrderNumber());

        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(CustomerAccountArchetypes.INVOICE_ITEM, id);
            result = (FinancialAct) service.get(reference);
            if (result != null && state.patient != null) {
                ActBean invoiceItemBean = new ActBean(result, service);
                IMObjectReference patient = invoiceItemBean.getNodeParticipantRef("patient");
                if (patient != null && !ObjectUtils.equals(state.patient.getObjectReference(), patient)) {
                    addNote(bean, "Patient is different to that in the original invoice. Was '"
                                  + ArchetypeQueryHelper.getName(patient, service) + "' (" + patient.getId() + ")"
                                  + ". Now '" + state.patient.getName() + "' (" + state.patient.getId() + ")");
                }
            }
            itemBean.setValue("sourceInvoiceItem", reference);
        } else {
            addNote(bean, "Unknown Placer Order Number: '" + orc.getPlacerOrderNumber().getEntityIdentifier() + "'");
        }
        return result;
    }

    /**
     * Adds a note if the dispense units are set, but don't match the product selling units.
     *
     * @param group   the order group
     * @param bean    the order bean
     * @param product the product
     */
    private void checkSellingUnits(RDS_O13_ORDER group, ActBean bean, Product product) {
        RXD rxd = group.getRXD();
        CE dispenseUnits = rxd.getActualDispenseUnits();
        String units = dispenseUnits.getIdentifier().getValue();
        IMObjectBean productBean = new IMObjectBean(product, service);
        String sellingUnits = productBean.getString("sellingUnits");
        if (!StringUtils.isEmpty(units) && !StringUtils.isEmpty(sellingUnits) && !units.equals(sellingUnits)) {
            String name = dispenseUnits.getText().getValue();
            if (name == null) {
                name = "";
            }
            addNote(bean, "Dispense Units (Id='" + units + "', name='" + name + "')"
                          + " do not match selling units (" + sellingUnits + ")");
        }
    }

    /**
     * Creates a new {@link State} using the PID segment.
     *
     * @param pid      the pid
     * @param location the practice location reference
     * @return a new state
     * @throws HL7Exception if the patient does not exist
     */
    private State getState(PID pid, IMObjectReference location) throws HL7Exception {
        Party patient = null;
        Party customer = null;
        long id = getId(pid.getPatientID());
        if (id != -1) {
            IMObjectReference reference = new IMObjectReference(PatientArchetypes.PATIENT, id);
            patient = getPatient(reference);
        }
        String note = null;
        if (patient == null) {
            XPN xpn = pid.getPatientName(0);
            String firstName = xpn.getGivenName().getValue();
            String lastName = xpn.getFamilyName().getSurname().getValue();
            String name;
            if (!StringUtils.isEmpty(lastName) && !StringUtils.isEmpty(firstName)) {
                name = firstName + " " + lastName;
            } else if (!StringUtils.isEmpty(lastName)) {
                name = lastName;
            } else {
                name = firstName;
            }
            note = "Unknown patient, Id='" + pid.getPatientID().getIDNumber() + "', name='" + name + "'";
        } else {
            customer = rules.getOwner(patient);
        }
        return new State(patient, customer, note, location);
    }

    /**
     * Adds a note to the notes node.
     *
     * @param bean  the act bean
     * @param value the note to add
     */
    private void addNote(ActBean bean, String value) {
        String notes = bean.getString("notes");
        if (!StringUtils.isEmpty(notes)) {
            notes += "\n" + value;
        } else {
            notes = value;
        }
        bean.setValue("notes", notes);
    }

    /**
     * Returns a patient given its reference.
     *
     * @param reference the patient reference
     * @return the corresponding patient or {@code null} if one is found
     */
    protected Party getPatient(IMObjectReference reference) {
        return (Party) service.get(reference);
    }

    /**
     * Helper to parse an id from an extended composite id.
     *
     * @param value the value
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    private long getId(CX value) {
        return getId(value.getIDNumber().getValue());
    }

    /**
     * Helper to parse an id from an entity identifier.
     *
     * @param value the value
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    private long getId(EI value) {
        return getId(value.getEntityIdentifier().getValue());
    }

    /**
     * Helper to parse an id from a coded element.
     *
     * @param value the value
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    private long getId(CE value) {
        return getId(value.getIdentifier().getValue());
    }

    /**
     * Helper to parse an id from a string.
     *
     * @param value the value to parse
     * @return the id, or {@code -1} if one doesn't exist or can't be parsed
     */
    private long getId(String value) {
        long id = -1;
        if (!StringUtils.isEmpty(value)) {
            try {
                id = Long.valueOf(value);
            } catch (NumberFormatException ignore) {
                // do nothing
            }
        }
        return id;
    }

    private class State {

        private ActBean orderBean;

        private ActBean returnBean;

        private final Party patient;

        private final Party customer;

        private final String note;

        private final IMObjectReference location;

        private final List<Act> acts = new ArrayList<Act>();

        public State(Party patient, Party customer, String note, IMObjectReference location) {
            this.patient = patient;
            this.customer = customer;
            this.note = note;
            this.location = location;
        }

        public ActBean getOrder() {
            if (orderBean == null) {
                orderBean = createParent(OrderArchetypes.PHARMACY_ORDER);
            }
            return orderBean;
        }

        public ActBean getReturn() {
            if (returnBean == null) {
                returnBean = createParent(OrderArchetypes.PHARMACY_RETURN);
            }
            return returnBean;
        }

        public ActBean createOrderItem() {
            return createItem(OrderArchetypes.PHARMACY_ORDER_ITEM, getOrder());
        }

        public ActBean createReturnItem() {
            return createItem(OrderArchetypes.PHARMACY_RETURN_ITEM, getReturn());
        }

        public List<Act> getActs() {
            return acts;
        }

        private ActBean createParent(String shortName) {
            Act act = (Act) service.create(shortName);
            ActBean bean = new ActBean(act, service);
            if (customer != null) {
                bean.addNodeParticipation("customer", customer);
            }
            if (location != null) {
                bean.addNodeParticipation("location", location);
            }
            if (note != null) {
                addNote(bean, note);
            }
            acts.add(act);
            return bean;
        }

        private ActBean createItem(String shortName, ActBean parent) {
            Act act = (Act) service.create(shortName);
            ActBean bean = new ActBean(act, service);
            if (patient != null) {
                bean.addNodeParticipation("patient", patient);
            }
            parent.addNodeRelationship("items", act);
            acts.add(act);
            return bean;
        }

    }

}
