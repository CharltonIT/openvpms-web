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

package org.openvpms.web.app.patient.mr;

import static org.openvpms.web.app.patient.mr.PatientRecordTypes.CLINICAL_EVENT;
import org.openvpms.web.app.subsystem.ShortNameList;


/**
 * CRUD Window for patient record acts in 'visit' view.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class VisitRecordCRUDWindow extends PatientRecordCRUDWindow {

    /**
     * Create a new <code>VisitRecordCRUDWindow</code>.
     */
    public VisitRecordCRUDWindow() {
        super(new ShortNameList(CLINICAL_EVENT));
    }

}
