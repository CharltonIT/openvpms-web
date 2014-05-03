/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openvpms.web.workspace.customer.estimate;

import static org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes.ESTIMATE;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
/**
 *
 * @author benjamincharlton
 */




public class CustomerEstimateQuery extends DateRangeActQuery<Act> {
    
    private static final ActStatuses STATUSES =  new ActStatuses(ESTIMATE);

   
    public CustomerEstimateQuery(Party customer) {
        super(customer, "customer", "participation.customer", new String[]{ESTIMATE}, STATUSES, Act.class);
    }
    
    
}
