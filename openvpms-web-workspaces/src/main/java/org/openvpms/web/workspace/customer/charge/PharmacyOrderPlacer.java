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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.pharmacy.PharmacyOrderService;
import org.openvpms.hl7.util.HL7Archetypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Places orders with the {@link PharmacyOrderService}, if a product is dispensed via a pharmacy.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderPlacer {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The location.
     */
    private final Party location;

    /**
     * The user responsible for the orders.
     */
    private final User user;

    /**
     * The cache.
     */
    private final IMObjectCache cache;

    /**
     * The orders, keyed on invoice item reference.
     */
    private Map<IMObjectReference, Order> orders = new HashMap<IMObjectReference, Order>();

    /**
     * The pharmacy order service.
     */
    private final PharmacyOrderService service;

    /**
     * The pharmacies.
     */
    private final Pharmacies pharmacies;

    /**
     * The patient context factory.
     */
    private final PatientContextFactory factory;

    /**
     * The patient information service, used to send updates when ordering when a patient isn't checked in.
     */
    private final PatientInformationService informationService;

    /**
     * Medical record rules, used to retrieve events.
     */
    private final MedicalRecordRules rules;


    /**
     * Constructs an {@link PharmacyOrderPlacer}.
     *
     * @param customer           the customer
     * @param location           the location
     * @param user               the user responsible for the orders
     * @param cache              the object cache
     * @param service            the pharmacy order service
     * @param pharmacies         the pharmacies
     * @param factory            the patient context factory
     * @param informationService the patient information service
     * @param rules              the medical record rules
     */
    public PharmacyOrderPlacer(Party customer, Party location, User user, IMObjectCache cache,
                               PharmacyOrderService service, Pharmacies pharmacies, PatientContextFactory factory,
                               PatientInformationService informationService, MedicalRecordRules rules) {
        this.customer = customer;
        this.location = location;
        this.user = user;
        this.cache = cache;
        this.service = service;
        this.pharmacies = pharmacies;
        this.factory = factory;
        this.informationService = informationService;
        this.rules = rules;
    }

    /**
     * Initialises the order placer with any existing orders.
     *
     * @param items the charge items
     */
    public void initialise(List<Act> items) {
        for (Act item : items) {
            initialise(item);
        }
    }

    /**
     * Initialises the order placer with an existing order.
     *
     * @param item the charge items
     */
    public void initialise(Act item) {
        Order order = getOrder(item);
        if (order != null) {
            orders.put(item.getObjectReference(), order);
        }
    }

    /**
     * Places any orders required by charge items.
     * <p/>
     * If items have been removed since initialisation, those items will be cancelled.
     *
     * @param items   the charge items
     * @param changes patient history changes, used to obtain patient events
     * @return the list of updated charge items
     */
    public List<Act> order(List<Act> items, PatientHistoryChanges changes) {
        List<IMObjectReference> ids = new ArrayList<IMObjectReference>(orders.keySet());
        List<Act> updated = new ArrayList<Act>();
        Set<Party> patients = new HashSet<Party>();
        for (Act act : items) {
            IMObjectReference id = act.getObjectReference();
            ids.remove(id);
            Order order = getOrder(act);
            Order existing = orders.get(id);
            if (order != null) {
                if (existing != null) {
                    if (needsCancel(existing, order)) {
                        // TODO - need to prevent this, as PlacerOrderNumbers should not be reused.
                        cancelOrder(existing, changes, patients);
                        if (createOrder(act, order, changes, patients)) {
                            updated.add(act);
                        }
                    } else if (needsUpdate(existing, order)) {
                        updateOrder(changes, order, patients);
                    }
                } else {
                    if (createOrder(act, order, changes, patients)) {
                        updated.add(act);
                    }
                }
                orders.put(id, order);
            } else if (existing != null) {
                // new product is not dispensed via a pharmacy.
                cancelOrder(existing, changes, patients);
            }
        }
        for (IMObjectReference id : ids) {
            Order existing = orders.remove(id);
            cancelOrder(existing, changes, patients);
        }
        return updated;
    }

    /**
     * Cancel orders.
     */
    public void cancel() {
        Map<IMObjectReference, Act> events = new HashMap<IMObjectReference, Act>();
        for (Order order : orders.values()) {
            PatientContext context = getPatientContext(order, events);
            if (context != null) {
                service.cancelOrder(context, order.getProduct(), order.getQuantity(),
                                    order.getId(), order.getStartTime(), order.getPharmacy(), user);
            }
        }
    }

    /**
     * Discontinue orders.
     */
    public void discontinue() {
        Map<IMObjectReference, Act> events = new HashMap<IMObjectReference, Act>();
        for (Order order : orders.values()) {
            PatientContext context = getPatientContext(order, events);
            if (context != null) {
                service.discontinueOrder(context, order.getProduct(), order.getQuantity(),
                                         order.getId(), order.getStartTime(), order.getPharmacy(), user);
            }
        }
    }


    private Order getOrder(Act act) {
        Order result = null;
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM)) {
            ActBean bean = new ActBean(act);
            Product product = (Product) getObject(bean.getNodeParticipantRef("product"));
            if (product != null && TypeHelper.isA(product, ProductArchetypes.MEDICATION,
                                                  ProductArchetypes.MERCHANDISE)) {
                Entity pharmacy = getPharmacy(product);
                if (pharmacy != null) {
                    Party patient = (Party) getObject(bean.getNodeParticipantRef("patient"));
                    if (patient != null) {
                        BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
                        User clinician = (User) getObject(bean.getNodeParticipantRef("clinician"));
                        IMObjectReference event = bean.getNodeSourceObjectRef("event");
                        result = new Order(act.getId(), act.getActivityStartTime(), product, patient, quantity,
                                           clinician, pharmacy, event);
                    }
                }
            }
        }
        return result;
    }

    private PatientContext getPatientContext(Order order, PatientHistoryChanges changes) {
        PatientContext result = null;
        List<Act> events = changes.getEvents(order.getPatient().getObjectReference());
        Act event;
        if (events == null || events.isEmpty()) {
            event = changes.getEvent(order.getEvent());
        } else {
            event = Collections.max(events, new Comparator<Act>() {
                @Override
                public int compare(Act o1, Act o2) {
                    return DateRules.compareDateTime(o1.getActivityStartTime(), o2.getActivityStartTime(), true);
                }
            });
        }
        if (event != null) {
            result = factory.createContext(order.getPatient(), customer, event, location, order.getClinician());
        }
        return result;
    }

    private PatientContext getPatientContext(Order order, Map<IMObjectReference, Act> events) {
        PatientContext result = null;
        Act event = events.get(order.getEvent());
        if (event == null) {
            event = (Act) getObject(order.getEvent());
        }
        if (event == null) {
            event = rules.getEvent(order.getPatient(), order.getStartTime());
        }
        if (event != null) {
            events.put(order.getEvent(), event);
        }
        if (event != null) {
            result = factory.createContext(order.getPatient(), customer, event, location, order.getClinician());
        }
        return result;
    }

    /**
     * Creates an order.
     *
     * @param act      the invoice item
     * @param order    the order
     * @param changes  the changes
     * @param patients tracks patients that have had notifications sent
     * @return {@code true} if an order was created (and invoice updated)
     */
    private boolean createOrder(Act act, Order order, PatientHistoryChanges changes, Set<Party> patients) {
        boolean result = false;
        PatientContext context = getPatientContext(order, changes);
        if (context != null) {
            notifyPatientInformation(context, changes, patients);
            if (service.createOrder(context, order.getProduct(), order.getQuantity(), order.getId(),
                                    order.getStartTime(), order.getPharmacy(), user)) {
                ActBean bean = new ActBean(act);
                bean.setValue("ordered", true);
                result = true;
            }
        }
        return result;
    }

    /**
     * Updates an order.
     *
     * @param order    the order
     * @param changes  the changes
     * @param patients tracks patients that have had notifications sent
     */
    private void updateOrder(PatientHistoryChanges changes, Order order, Set<Party> patients) {
        PatientContext context = getPatientContext(order, changes);
        if (context != null) {
            notifyPatientInformation(context, changes, patients);
            service.updateOrder(context, order.getProduct(), order.getQuantity(), order.getId(), order.getStartTime(),
                                order.getPharmacy(), user);
        }
    }

    /**
     * Cancel an order.
     *
     * @param order    the order
     * @param changes  the changes
     * @param patients the patients, used to prevent duplicate patient update notifications being sent
     */
    private void cancelOrder(Order order, PatientHistoryChanges changes, Set<Party> patients) {
        PatientContext context = getPatientContext(order, changes);
        if (context != null) {
            notifyPatientInformation(context, changes, patients);
            service.cancelOrder(context, order.getProduct(), order.getQuantity(), order.getId(), order.getStartTime(),
                                order.getPharmacy(), user);
        }
    }

    /**
     * Notifies registered listeners of patient visit information, when placing orders outside of a current visit.
     * <p/>
     * This is required for listeners that remove patient information when a patient is discharged.
     * <p/>
     * The {@code patients} variable is used to track if patient information has already been sent for a given patient,
     * to avoid multiple notifications being sent. This isn't kept across calls to {@link #order}, so
     * redundant notifications may be sent.
     *
     * @param context  the context
     * @param changes  the patient history changes
     * @param patients tracks patients that have had notifications sent
     */
    private void notifyPatientInformation(PatientContext context, PatientHistoryChanges changes, Set<Party> patients) {
        Act visit = context.getVisit();
        if (!patients.contains(context.getPatient())) {
            if (changes.isNew(visit)
                || (visit.getActivityEndTime() != null
                    && DateRules.compareTo(visit.getActivityEndTime(), new Date()) < 0)) {
                informationService.updated(context, user);
                patients.add(context.getPatient());
            }
        }
    }

    /**
     * Determines if an existing order needs updating.
     *
     * @param existing the existing order
     * @param order    the new order
     * @return {@code true} if the existing order needs updating
     */
    private boolean needsUpdate(Order existing, Order order) {
        return existing.getQuantity().compareTo(order.getQuantity()) != 0
               || !ObjectUtils.equals(existing.getClinician(), order.getClinician());
    }

    /**
     * Determines if an existing order needs cancelling.
     *
     * @param existing the existing order
     * @param order    the new order
     * @return {@code true} if the existing order needs cancelling
     */
    private boolean needsCancel(Order existing, Order order) {
        return !ObjectUtils.equals(existing.getPatient(), order.getPatient())
               || !ObjectUtils.equals(existing.getProduct(), order.getProduct())
               || !ObjectUtils.equals(existing.getPharmacy(), order.getPharmacy());
    }

    /**
     * Returns the pharmacy for a product and location.
     *
     * @param product the product
     * @return the pharmacy, or {@code null} if none is present
     */
    private Entity getPharmacy(Product product) {
        IMObjectBean bean = new IMObjectBean(product);
        Entity pharmacy = (Entity) getObject(bean.getNodeTargetObjectRef("pharmacy"));
        if (pharmacy == null) {
            // use the pharmacy linked to the product type, if present
            Entity type = (Entity) getObject(bean.getNodeSourceObjectRef("type"));
            if (type != null) {
                IMObjectBean typeBean = new IMObjectBean(type);
                pharmacy = (Entity) getObject(typeBean.getNodeTargetObjectRef("pharmacy"));
            }
        }
        if (pharmacy != null && TypeHelper.isA(pharmacy, HL7Archetypes.PHARMACY_GROUP)) {
            pharmacy = pharmacies.getPharmacy(pharmacy, location.getObjectReference());
        }
        return pharmacy;
    }

    /**
     * Returns an object given its reference.
     *
     * @param reference the reference. May be {@code null}
     * @return the object, or {@code null} if none is found
     */
    private IMObject getObject(IMObjectReference reference) {
        return (reference != null) ? cache.get(reference) : null;
    }

    private static class Order {

        private final long id;

        private final Date startTime;

        private Product product;

        private final Party patient;

        private final BigDecimal quantity;

        private final Entity pharmacy;

        private final User clinician;

        private final IMObjectReference event;

        public Order(long id, Date startTime, Product product, Party patient, BigDecimal quantity, User clinician,
                     Entity pharmacy, IMObjectReference event) {
            this.id = id;
            this.startTime = startTime;
            this.product = product;
            this.patient = patient;
            this.quantity = quantity;
            this.pharmacy = pharmacy;
            this.clinician = clinician;
            this.event = event;
        }

        public Party getPatient() {
            return patient;
        }

        public Entity getPharmacy() {
            return pharmacy;
        }

        public Product getProduct() {
            return product;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public long getId() {
            return id;
        }

        public User getClinician() {
            return clinician;
        }

        public IMObjectReference getEvent() {
            return event;
        }

        public Date getStartTime() {
            return startTime;
        }
    }

}
