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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.patientClinicalProblem</em>.
 * This prevents the editing of items nodes in 'visits view'.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ClinicalProblemActEditor extends ActEditor {

    /**
     * Construct a new <code>ClinicalProblemActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>.
     */
    public ClinicalProblemActEditor(Act act, Act parent,
                                    LayoutContext context) {
        this(act, parent, (parent == null), context);
        // disable editing of the items node if there is a parent act.
    }

    /**
     * Construct a new <code>ClinicalProblemActEditor</code>.
     *
     * @param act       the act to edit
     * @param parent    the parent act. May be <code>null</code>
     * @param editItems if <code>true</code> create an editor for any items node
     * @param context   the layout context. May be <code>null</code>.
     */
    public ClinicalProblemActEditor(Act act, Act parent, boolean editItems,
                                    LayoutContext context) {
        super(act, parent, editItems, context);
        // disable editing of the items node if there is a parent act.

        initParticipant("patient", context.getContext().getPatient());
    }

    /**
     * Update totals when an act item changes.
     * <p/>
     * todo - workaround for OVPMS-211
     */
    protected void updateTotals() {
    }
}
