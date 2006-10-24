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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.consult;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskContextImpl;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;


/**
 * Consult workflow.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ConsultWorkflow extends WorkflowImpl {

    /**
     * The event short name.
     */
    public static final String EVENT_SHORTNAME = "act.patientClinicalEvent";

    /**
     * The initial context.
     */
    private TaskContext initial;


    /**
     * Constructs a new <code>ConsultWorkflow</code> from an
     * <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act the act
     */
    public ConsultWorkflow(Act act) {
        // update the act status
        TaskProperties appProps = new TaskProperties();
        appProps.add("status", ActStatus.IN_PROGRESS);
        addTask(new UpdateIMObjectTask(act, appProps));

        ActBean bean = new ActBean(act);
        Party patient = (Party) bean.getParticipant("participation.patient");
        final User clinician
                = (User) bean.getParticipant("participation.clinician");

        initial = new TaskContextImpl();
        initial.setPatient(patient);
        initial.setClinician(clinician);

        // get/create the clinical event, and edit it
        addTask(new PatientClinicalEventTask());
        addTask(new EditIMObjectTask(EVENT_SHORTNAME));
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

}
