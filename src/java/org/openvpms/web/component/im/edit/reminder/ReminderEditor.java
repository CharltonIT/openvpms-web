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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.reminder;

import org.openvpms.archetype.rules.patient.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ErrorHelper;

/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.patientReminder</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public class ReminderEditor extends AbstractActEditor {

    /**
     * Construct a new <code>ReminderEditor</code>.
     *
     * @param act
     * @param parent
     * @param context
     */
    public ReminderEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.patientReminder")) {
            throw new IllegalArgumentException(
                    "Invalid act type:" + act.getArchetypeId().getShortName());
        }
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        Editor editor = getEditor("reminderType");

        // add a listener to update the due date when the reminder type is modified
        ModifiableListener listener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onReminderTypeChanged();
            }
        };
        if (editor != null)
            editor.addModifiableListener(listener);
    }

    /**
     * Updates the Due Date based on the reminderType reminder interval.
     */
    private void onReminderTypeChanged() {
        try {
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            ReminderRules.calculateReminderDueDate((Act) getObject(), service);
            Property property = getProperty("endTime");
            property.refresh();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
