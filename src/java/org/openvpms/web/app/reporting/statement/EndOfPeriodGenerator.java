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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.reporting.statement;

import org.openvpms.archetype.component.processor.Processor;
import org.openvpms.archetype.rules.finance.statement.EndOfPeriodProcessor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * End-of-period generator.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class EndOfPeriodGenerator extends AbstractStatementGenerator {

    /**
     * The statement processor progress bar.
     */
    private StatementProgressBarProcessor progressBarProcessor;


    /**
     * Creates a new <tt>EndOfPeriodGenerator</tt>.
     *
     * @param date the statement date
     */
    public EndOfPeriodGenerator(Date date) {
        super(Messages.get("reporting.statements.eop.title"),
              Messages.get("reporting.statements.eop.cancel.title"),
              Messages.get("reporting.statements.eop.cancel.message"));
        ArchetypeQuery query
                = new ArchetypeQuery("party.customer*", false, false);
        int size = countCustomers(query);
        query.setMaxResults(1000);

        IterableIMObjectQuery<Party> customers
                = new IterableIMObjectQuery<Party>(query);
        Processor<Party> processor = new EndOfPeriodProcessor(
                date, ArchetypeServiceHelper.getArchetypeService());
        progressBarProcessor = new StatementProgressBarProcessor(processor,
                                                                 customers,
                                                                 size);
    }

    /**
     * Returns the processor.
     *
     * @return the processor
     */
    protected StatementProgressBarProcessor getProcessor() {
        return progressBarProcessor;
    }

    /**
     * Counts the available customers matching a query.
     *
     * @param query the query
     * @return the no. of customers
     */
    private int countCustomers(ArchetypeQuery query) {
        query.setMaxResults(0);
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        query.setCountResults(true);
        IPage<IMObject> page = service.get(query);
        query.setCountResults(false);
        return page.getTotalResults();
    }

}
