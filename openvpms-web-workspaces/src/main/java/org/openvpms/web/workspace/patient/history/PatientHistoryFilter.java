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

package org.openvpms.web.workspace.patient.history;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.act.ActHierarchyFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Filters patient history.
 * <p/>
 * This:
 * <ul>
 * <li>enables specific event items to by included by archetype</li>
 * <li>excludes charge items if they are linked to by an included medication</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class PatientHistoryFilter extends ActHierarchyFilter<Act> {

    /**
     * The short names of the child acts to return.
     */
    private final List<String> shortNames;

    /**
     * Determines if invoice items should be included. If {@code true}, this excludes those invoice items linked to
     * <em>act.patientMedication</em>
     */
    private final boolean invoice;

    /**
     * Constructs a {@link PatientHistoryFilter}.
     * <p/>
     * Items are sorted on ascending timestamp.
     *
     * @param shortNames the history item short names to include
     */
    public PatientHistoryFilter(String[] shortNames) {
        this(shortNames, true);
    }

    /**
     * Constructs a {@link PatientHistoryFilter}.
     *
     * @param shortNames    the history item short names to include
     * @param sortAscending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    public PatientHistoryFilter(String[] shortNames, boolean sortAscending) {
        super();
        this.shortNames = new ArrayList<String>(Arrays.asList(shortNames));
        invoice = this.shortNames.remove(CustomerAccountArchetypes.INVOICE_ITEM);
        setSortItemsAscending(sortAscending);
    }

    /**
     * Filters child acts.
     *
     * @param parent   the parent act
     * @param children the child acts
     * @return the filtered acts
     */
    @Override
    protected List<Act> filter(Act parent, List<Act> children) {
        List<Act> result;
        if (invoice && TypeHelper.isA(parent, PatientArchetypes.CLINICAL_EVENT)) {
            result = filterInvoiceItems(parent, children);
        } else {
            result = children;
        }
        return result;
    }

    /**
     * Determines if a child act should be included.
     * <p/>
     * This implementation excludes children of <em>act.patientClinicalProblem</em> acts that are linked to an event
     * different to the root.
     *
     * @param child  the child act
     * @param parent the parent act
     * @param root   the root act
     * @return {@code true} if the child act should be included
     */
    @Override
    protected boolean include(Act child, Act parent, Act root) {
        if (TypeHelper.isA(parent, PatientArchetypes.CLINICAL_PROBLEM)) {
            ActBean bean = new ActBean(child);
            IMObjectReference event = bean.getNodeSourceObjectRef("event");
            if (event != null && event.getId() != root.getId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Filters relationships.
     *
     * @param act the act
     * @return the filtered relationships
     */
    @Override
    protected Collection<ActRelationship> getRelationships(Act act) {
        String[] acts = shortNames.toArray(new String[shortNames.size()]);
        return getRelationships(act.getSourceActRelationships(), createIsA(acts, true));
    }

    /**
     * Excludes invoice items if there is a medication act that links to it.
     *
     * @param event    the <em>act.patientClinicalEvent</em>
     * @param children the included child acts
     * @return the child acts with invoice items added where there is no corresponding medication linking to it
     */
    private List<Act> filterInvoiceItems(Act event, List<Act> children) {
        List<Act> result;
        result = new ArrayList<Act>(children);
        Set<IMObjectReference> chargeItemRefs = new HashSet<IMObjectReference>();
        ActBean bean = new ActBean(event);
        for (ActRelationship relationship : bean.getRelationships(PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM)) {
            IMObjectReference target = relationship.getTarget();
            if (target != null) {
                chargeItemRefs.add(target);
            }
        }
        if (!chargeItemRefs.isEmpty()) {
            for (int i = 0; i < children.size() && !chargeItemRefs.isEmpty(); ++i) {
                Act act = children.get(i);
                if (TypeHelper.isA(act, PatientArchetypes.PATIENT_MEDICATION)) {
                    ActBean medication = new ActBean(act);
                    List<IMObjectReference> chargeItem = medication.getNodeSourceObjectRefs("invoiceItem");
                    if (!chargeItem.isEmpty()) {
                        chargeItemRefs.remove(chargeItem.get(0));
                    }
                }
            }
            List<Act> chargeItems = ActHelper.getActs(chargeItemRefs);
            result.addAll(chargeItems);
        }
        return result;
    }

}
