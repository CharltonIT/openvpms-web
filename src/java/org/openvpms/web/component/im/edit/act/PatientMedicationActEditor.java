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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>act.patientMedication</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientMedicationActEditor extends AbstractActEditor {

    /**
     * Construct a new <code>PatientMedicationActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context. May be <code>null</code>
     */
    public PatientMedicationActEditor(Act act, Act parent,
                                      LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.patientMedication")) {
            throw new IllegalArgumentException("Invalid act type:"
                    + act.getArchetypeId().getShortName());
        }

        ActBean bean = new ActBean(parent);
        if (bean.hasNode("product")) {
            // update the product from the parent
            IMObjectReference product
                    = bean.getParticipantRef("participation.product");
            if (TypeHelper.isA(product, "product.medication")) {
                setProduct(product);
            } else {
                setProduct(null);
            }
        }
        setPatient(bean.getParticipantRef("participation.patient"));
    }

    /**
     * Sets the product.
     *
     * @param product the product reference. May be <code>null</code>
     */
    public void setProduct(IMObjectReference product) {
        setParticipant("product", product);
    }

    /**
     * Returns the product.
     *
     * @return the product refereence. May be <code>null</code>
     */
    public IMObjectReference getProduct() {
        return getParticipantRef("product");
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient reference. May be <code>null</code>
     */
    public void setPatient(IMObjectReference patient) {
        setParticipant("patient", patient);
    }

    /**
     * Returns the patient.
     *
     * @return the patient reference. May be <code>null</code>
     */
    public IMObjectReference getPatient() {
        return getParticipantRef("patient");
    }

}
