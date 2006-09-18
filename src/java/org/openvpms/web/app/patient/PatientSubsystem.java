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

package org.openvpms.web.app.patient;

import org.openvpms.web.app.patient.document.PatientDocumentWorkspace;
import org.openvpms.web.app.patient.mr.PatientRecordWorkspace;
import org.openvpms.web.app.patient.reminder.ReminderWorkspace;
import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Patient sybsystem.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>PatientSubsystem</code>.
     */
    public PatientSubsystem() {
        super("patient");
        addWorkspace(new InformationWorkspace());
        addWorkspace(new PatientRecordWorkspace());
        addWorkspace(new PatientDocumentWorkspace());
        addWorkspace(new DummyWorkspace("patient", "investigation"));
        addWorkspace(new ReminderWorkspace());
    }
}
