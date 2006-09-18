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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Messaging CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MessagingCRUDWindow extends AbstractViewCRUDWindow {

    /**
     * The 'forward' button.
     */
    private Button forward;

    /**
     * The 'completed' button.
     */
    private Button completed;


    /**
     * Constructs a new <code>MessagingCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public MessagingCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        if (forward == null) {
            forward = ButtonFactory.create("forward", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onForward();
                }
            });
        }

        if (completed == null) {
            completed = ButtonFactory.create("completed", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onCompleted();
                }
            });
        }
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        Row buttons = getButtons();
        buttons.removeAll();
        if (enable) {
            buttons.add(getCreateButton());
            buttons.add(forward);
            buttons.add(completed);
            buttons.add(getPrintButton());
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        onRefresh(object);
    }

    /**
     * Invoked when the 'forward' button is pressed.
     */
    private void onForward() {
        String title = Messages.get("workflow.messaging.forward.title");
        final SelectUserDialog dialog = new SelectUserDialog(title);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (dialog.getUser() != null) {
                    forward(dialog.getUser());
                }
            }
        });
        dialog.show();
    }

    /**
     * Forwards a message to a particular user.
     *
     * @param user the user to forward to
     */
    private void forward(User user) {
        try {
            ActBean bean = new ActBean((Act) getObject());
            bean.setParticipant("participation.user", user);
            bean.save();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        onRefresh(getObject());
    }

    /**
     * Invoked when the 'completed' button is pressed.
     */
    private void onCompleted() {
        Act act = (Act) getObject();
        if (!"Completed".equals(act.getStatus())) {
            act = (Act) IMObjectHelper.reload(act);
            if (act != null) {
                act.setStatus("Completed");
                SaveHelper.save(act);
            }
            onRefresh(act);
        }
    }
}
