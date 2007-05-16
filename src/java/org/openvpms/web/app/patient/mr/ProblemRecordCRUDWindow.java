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
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import static org.openvpms.web.app.patient.mr.PatientRecordTypes.CLINICAL_PROBLEM;
import static org.openvpms.web.app.patient.mr.PatientRecordTypes.RELATIONSHIP_CLINICAL_EVENT_ITEM;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.List;


/**
 * CRUD Window for patient record acts in 'problem' view.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProblemRecordCRUDWindow extends PatientRecordCRUDWindow {

    /**
     * Constructs a new <tt>ProblemRecordCRUDWindow</tt>.
     */
    public ProblemRecordCRUDWindow() {
        super(new ShortNameList(CLINICAL_PROBLEM));
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.removeAll();
        if (enable) {
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            buttons.add(getPrintButton());
        } else if (getEvent() != null) {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param act   the act
     * @param isNew determines if the object is a new instance
     */
    @Override
    protected void onSaved(Act act, boolean isNew) {
        Act event = IMObjectHelper.reload(getEvent());
        if (event != null) {
            try {
                boolean save = false;
                ActBean bean = new ActBean(act);
                ActBean eventBean = new ActBean(event);

                // if the problem has no parent event, add it
                if (!hasTargetRelationship(act,
                                           RELATIONSHIP_CLINICAL_EVENT_ITEM)) {
                    eventBean.addRelationship(RELATIONSHIP_CLINICAL_EVENT_ITEM,
                                              act);
                    save = true;
                }

                // for each of the problem's child acts, link them to the
                // parent event
                List<Act> acts = bean.getActs();
                String[] shortNames = getClinicalEventItemShortNames();
                for (Act child : acts) {
                    if (TypeHelper.isA(child, shortNames)
                            && !hasTargetRelationship(child, event)) {
                        eventBean.addRelationship(
                                RELATIONSHIP_CLINICAL_EVENT_ITEM, child);
                        save = true;
                    }
                }
                if (save) {
                    eventBean.save();
                }
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }

        }
        super.onSaved(act, isNew);
    }

    /**
     * Determines if an act is a target in an act relationship.
     *
     * @param act  the target act
     * @param type relationship type
     * @return <code>true</code> if there is an act relationship of the specified
     *         type with the act as the target
     */
    protected boolean hasTargetRelationship(Act act, String type) {
        for (ActRelationship rel : act.getTargetActRelationships()) {
            if (TypeHelper.isA(rel, type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if an act is a target in an act relationship.
     *
     * @param target the target act
     * @param source the source act
     * @return <code>true</code> if there is an act relationship between source
     *         and target with target as the target act
     */
    protected boolean hasTargetRelationship(Act target, Act source) {
        IMObjectReference sourceRef = source.getObjectReference();
        for (ActRelationship rel : target.getTargetActRelationships()) {
            if (rel.getSource().equals(sourceRef)) {
                return true;
            }
        }
        return false;
    }

}
