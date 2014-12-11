/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.doc.DocumentActEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>act.patientDocument*</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientDocumentActEditor extends DocumentActEditor {

    /**
     * Constructs a <tt>PatientDocumentActEditor</tt>.
     *
     * @param act     the act
     * @param parent  the parent
     * @param context the layout context
     */
    public PatientDocumentActEditor(DocumentAct act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        boolean initPatient = false;
        if (parent != null && parent instanceof Act) {
            ActBean bean = new ActBean((Act) parent);
            if (bean.hasNode("patient")) {
                initParticipant("patient", bean.getParticipantRef(PatientArchetypes.PATIENT_PARTICIPATION));
                initPatient = true;
            }
        }
        if (!initPatient) {
            initParticipant("patient", context.getContext().getPatient());
        }
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PatientDocumentActLayoutStrategy(getDocumentEditor(), getVersionsEditor());
    }

}
