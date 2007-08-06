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

import org.openvpms.archetype.component.processor.ProcessorListener;
import org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes;
import org.openvpms.archetype.rules.finance.statement.StatementEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Prints statements.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class StatementPrintProcessor implements ProcessorListener<StatementEvent> {

    /**
     * The batch processor to invoke to process the next statement, when
     * printing interactively
     */
    private final StatementProgressBarProcessor processor;


    /**
     * Constructs a new <tt>StatementPrintProcessor</tt>.
     *
     * @param processor the batch processor to invoke to process the next
     *                  statement, when printing interactively.
     */
    public StatementPrintProcessor(StatementProgressBarProcessor processor) {
        this.processor = processor;
    }

    /**
     * Process a statement.
     *
     * @param event the event
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void process(StatementEvent event) {
        print(event.getCustomer(), event.getDate(), event.getActs());
    }

    /**
     * Prints a statement.
     *
     * @param customer      the customer
     * @param statementDate the statement date
     * @param acts          the acts to print
     */
    private void print(final Party customer, Date statementDate,
                       Iterable<Act> acts) {
        IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<Act>(
                acts, CustomerAccountActTypes.OPENING_BALANCE);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("statementDate", statementDate);
        printer.setParameters(params);

        String title = Messages.get("reporting.statements.print.customer");
        InteractiveIMPrinter<Act> iPrinter
                = new InteractiveIMPrinter<Act>(title, printer);
        iPrinter.setListener(new PrinterListener() {
            public void printed() {
                try {
                    processor.processCompleted(customer);
                    processor.process(); // process the next statement
                } catch (OpenVPMSException exception) {
                    processor.notifyError(exception);
                }
            }

            public void cancelled() {
                processor.setStatus(null);
            }

            public void skipped() {
            }

            public void failed(Throwable cause) {
                processor.notifyError(cause);
            }
        });
        processor.setSuspend(true); // suspend generation while printing
        iPrinter.print();
    }

}
