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

package org.openvpms.web.app.workflow.messaging;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.select.QuerySelectorListener;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Dialog to select a user.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SelectUserDialog extends PopupDialog {

    /**
     * The selector.
     */
    private final IMObjectSelector selector;


    /**
     * Construct a new <code>SelectUserDialog</code>.
     *
     * @param title the window title
     */
    public SelectUserDialog(String title) {
        super(title, "SelectUserDialog", CANCEL);

        String shortName = "security.user";
        String type = DescriptorHelper.getDisplayName(shortName);
        selector = new IMObjectSelector(type, new String[]{shortName});
        selector.setListener(new QuerySelectorListener() {
            public void selected(IMObject object) {
                onSelected(object);
            }
        });

        Label label = LabelFactory.create("username");
        Row row = RowFactory.create("CellSpacing", label,
                                    selector.getComponent());
        getLayout().add(row);
    }

    /**
     * Returns the selected user.
     *
     * @return the selected user
     */
    public User getUser() {
        return (User) selector.getObject();
    }

    /**
     * Invoked when a user is selected. If non-null, closes the dialog.
     *
     * @param object the selected user. May be <code>null</code>
     */
    private void onSelected(IMObject object) {
        if (object != null) {
            close();
        }
    }


}
