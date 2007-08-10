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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.component.processor.Processor;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Processes statements, displaying progress in a progress bar.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class StatementProgressBarProcessor extends ProgressBarProcessor<Party> {

    /**
     * The processor to delegate to.
     */
    private final Processor<Party> processor;

    /**
     * The customer label.
     */
    private Label status;

    /**
     * The component representing this.
     */
    private Component component;


    /**
     * Constructs a new <tt>StatementProgressBarProcessor</tt>.
     *
     * @param processor the statement processor
     * @param customers the customers to process
     */
    public StatementProgressBarProcessor(Processor<Party> processor,
                                         List<Party> customers) {
        this(processor, customers, customers.size());
    }

    /**
     * Constructs a new <tt>StatementProgressBarProcessor</tt>.
     *
     * @param processor the statement processor
     * @param customers the customers to process
     * @param size      the expected no. of items. This need not be exact
     */
    public StatementProgressBarProcessor(Processor<Party> processor,
                                         Iterable<Party> customers,
                                         int size) {
        super(customers, size, null);
        this.processor = processor;
        status = LabelFactory.create();
        component = ColumnFactory.create("CellSpacing", getProgressBar(),
                                         status);
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        return component;
    }

    /**
     * To be invoked when processing of an object is complete.
     * This periodically updates the progress bar.
     *
     * @param customer the processed customer
     */
    public void processCompleted(Party customer) {
        status.setText(null);
        super.processCompleted(customer);
    }

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    @Override
    public void notifyError(Throwable exception) {
        super.notifyError(exception);
    }

    /**
     * Sets the processing status.
     *
     * @param status the status message. May be <tt>null</tt>
     */
    public void setStatus(String status) {
        this.status.setText(status);
    }

    /**
     * Processes a customer.
     *
     * @param customer the customer to process
     */
    protected void process(Party customer) {
        String message = Messages.get("reporting.statements.processing",
                                      customer.getName());
        setStatus(message);
        processor.process(customer);
        if (!isSuspended()) {
            processCompleted(customer);
        }
    }
}
