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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.customer.document;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.patient.mr.PatientDocumentQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.TabbedBrowser;
import org.openvpms.web.resource.util.Messages;

/**
 * A browser for customer and patient documents.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerPatientDocumentBrowser extends TabbedBrowser<Act> {

    /**
     * The customer. May be <tt>null</tt>
     */
    private final Party customer;

    /**
     * The patient. May be <tt>null</tt>
     */
    private final Party patient;

    /**
     * The customer document browser.
     */
    private Browser<Act> customerDocuments;

    /**
     * The patient document browser.
     */
    private Browser<Act> patientDocuments;

    /**
     * Constructs a <tt>CustomerPatientDocumentBrowser</tt>.
     *
     * @param customer the customer. May be <tt>null</tt>
     * @param patient  the patient. May be <tt>null</tt>
     */
    public CustomerPatientDocumentBrowser(Party customer, Party patient) {
        this.customer = customer;
        this.patient = patient;
    }

    /**
     * Returns the browser component.
     *
     * @return the browser component
     */
    public Component getComponent() {
        if (customerDocuments == null && customer != null) {
            CustomerDocumentQuery<Act> query = new CustomerDocumentQuery<Act>(customer);
            customerDocuments = BrowserFactory.create(query);
            addBrowser(Messages.get("customer.documentbrowser.customer"), customerDocuments);
        }
        if (patientDocuments == null && patient != null) {
            PatientDocumentQuery query = new PatientDocumentQuery(patient);
            patientDocuments = BrowserFactory.create(query);
            addBrowser(Messages.get("customer.documentbrowser.patient"), patientDocuments);
        }
        return super.getComponent();
    }

}