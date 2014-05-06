/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openvpms.web.workspace.customer.estimate;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.openvpms.archetype.rules.act.EstimateActStatus;
import static org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes.ESTIMATE;
import org.openvpms.archetype.rules.finance.estimate.EstimateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
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
    /**
     * Constructs and Estimate Query 
     * @param customer 
     */
     private final IArchetypeService service;
     
     private final EstimateRules rules;
    
    public CustomerEstimateQuery(Party customer) {
        super(ESTIMATE);
        rules = ServiceHelper.getBean(EstimateRules.class);
        service = ServiceHelper.getArchetypeService();
        this.add(join("customer").add(eq("entity",customer.getObjectReference())));
        this.add(ne("status", EstimateActStatus.CANCELLED));
        this.add(ne("status", EstimateActStatus.INVOICED));
        this.add(or(isNull("endTime"), gt("endTime", new Date())));
    }
    public IPage query(){
        IPage<IMObject> result = service.get(this);
        return result;
    }
    
    private Iterable<Act> objectList(){
        return new IterableIMObjectQuery<Act>(service, this);
    }
            
    
    public Boolean hasEstimates(){
        this.setCountResults(true);
        return this.query().getTotalResults() != 0;
    }
    
    public Boolean patientHasEstimates(Party patient){
        Iterator<Act> iterator = this.objectList().iterator();
        while (iterator.hasNext()){
           Boolean result = rules.isPatientEstimate(iterator.next(), patient);
           if(result) {
               return result;
           }
        }
        return false;
    }
            
    
    
}
