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

package org.openvpms.web.app.patient.mr;

import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.subsystem.DocumentCRUDWindow;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.view.IMObjectViewer;

/**
 * CRUD window for patient documents.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class PatientDocumentCRUDWindow extends DocumentCRUDWindow {

    /**
     * Constructs a <tt>PatientDocumentCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public PatientDocumentCRUDWindow(Archetypes<DocumentAct> archetypes) {
        super(archetypes);
    }

    /**
     * Creates a new {@link IMObjectViewer} for an object.
     *
     * @param object the object to view
     * @return a new viewer
     */
    protected IMObjectViewer createViewer(IMObject object) {
        if (TypeHelper.isA(object, InvestigationArchetypes.PATIENT_INVESTIGATION)) {
            // disable printing from the viewer, as it is enabled by the CRUD window
            PatientInvestigationActLayoutStrategy print = new PatientInvestigationActLayoutStrategy();
            print.setEnableButton(false);
            LayoutContext context = new DefaultLayoutContext();
            context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);
            return new IMObjectViewer(object, null, print, context);
        } else {
            return super.createViewer(object);
        }
    }
}