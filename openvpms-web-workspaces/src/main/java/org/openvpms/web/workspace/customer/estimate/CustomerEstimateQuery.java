/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openvpms.web.workspace.customer.estimate;

import static org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes.ESTIMATE;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;
/**
 * Queries a customers estimates.
 * 
 * @author benjamincharlton
 */

public class CustomerEstimateQuery extends DateRangeActQuery<Act> {
    
    private static final ActStatuses STATUSES =  new ActStatuses(ESTIMATE);

    private static final SortConstraint[] DEFAULT_SORT = {
        new VirtualNodeSortConstraint("startTime",false)};
   
    public CustomerEstimateQuery(Party customer) {
        super(customer, "customer", "participation.customer", new String[]{ESTIMATE}, STATUSES, Act.class);
        setDefaultSortConstraint(DEFAULT_SORT);
    }
    
    
}
