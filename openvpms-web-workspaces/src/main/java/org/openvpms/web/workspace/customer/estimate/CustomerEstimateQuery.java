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
 *
 */
package org.openvpms.web.workspace.customer.estimate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.openvpms.archetype.rules.act.EstimateActStatus;
import static org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes.ESTIMATE;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gt;
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.ne;
import static org.openvpms.component.system.common.query.Constraints.or;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;
import org.openvpms.web.system.ServiceHelper;

/**
 * Queries a customers estimates.
 *
 * @author benjamincharlton
 */
public class CustomerEstimateQuery extends ArchetypeQuery {

    private final IArchetypeService service;

    /**
     * Constructs the Estimate QUERY
     *
     * @param customer
     */
    public CustomerEstimateQuery(Party customer) {
        super(ESTIMATE);
        service = ServiceHelper.getArchetypeService();
        this.add(join("customer").add(eq("entity", customer.getObjectReference())));
        this.add(ne("status", EstimateActStatus.CANCELLED));
        this.add(ne("status", EstimateActStatus.INVOICED));
        this.add(or(isNull("endTime"), gt("endTime", new Date())));
    }

    /**
     * Returns a Paged set of IMObjects as a result.
     * @return {@code IPage<IMObject>}
     */
    public IPage query() {
        IPage<IMObject> result = service.get(this);
        return result;
    }
    /**
     * Returns an iterable ret of Acts
     * @return {@code IterableObject<Act>}
     */
    private Iterable<Act> objectList() {
        return new IterableIMObjectQuery<Act>(service, this);
    }

    /**
     * Constructs and List of estimate Acts returned by the query
     * @return (@code ArrayList<Act>}
     */
    public List<Act> resultList() {
        return this.resultList(null);
    }

    /**
     * Constructs and List of estimate Acts returned by the query
     * @param patient optional can be null if so returns customer estimates
     * @return (@code ArrayList<Act>} of Estimates
     */
    public List<Act> resultList(Party patient) {
        if (patient != null) {
            this.add(join("items").add(join("target").add(join("patient").add(eq("entity", patient.getObjectReference())))));
        }
        Iterator<Act> iterator = this.objectList().iterator();
        List<Act> list = new ArrayList<Act>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    


/**
 * Returns if a customer has Estimates
 *
 * @return {@code True} if estimates exist
 */
public Boolean hasEstimates() {
        return hasEstimates(null);
    }

    /**
     * Returns if a patient has estimates
     * @param patient can be null if so returns the customer estimates.
     * @return {@code True} if estimates exist for the patient.
     */
    public Boolean hasEstimates(Party patient) {
        if (patient != null) {
            this.add(join("items").add(join("target").add(join("patient").add(eq("entity", patient.getObjectReference())))));
        }
        this.setCountResults(true);
        return this.query().getTotalResults() != 0;
    }

}
