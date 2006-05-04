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

package org.openvpms.web.app.login;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.web.app.ApplicationContentPane;
import org.openvpms.web.app.OpenVPMSApp;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.resource.util.Messages;

/**
 * Enter description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LoginDialog extends PopupDialog {

    /**
     * The username field.
     */
    private TextField _username;

    /**
     * The password field.
     */
    private TextField _password;

    /**
     * Dialog style name.
     */
    private static final String STYLE = "LoginDialog";

    /**
     * Label style name.
     */
    private static final String LABEL_STYLE = "LoginDialog.Label";

    /**
     * Layout grid style.
     */
    private static final String GRID_STYLE = "LoginDialog.Grid";

    /**
     * Login title localisation key.
     */
    private static final String LOGIN_KEY = "title.login";

    /**
     * User label localisation key.
     */
    private static final String USER_KEY = "username";

    /**
     * Password label localisation key.
     */
    private static final String PASSWORD_KEY = "password";

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(LoginPane.class);


    /**
     * Construct a new <code>LoginDialog</code>.
     */
    public LoginDialog() {
        super(Messages.get(LOGIN_KEY), STYLE, Buttons.OK);
        setClosable(false);

        _username = TextComponentFactory.create();
        _username.setText("guest");
        _password = TextComponentFactory.createPassword();

        _username.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setFocus(_password);
            }
        });
        _password.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onOK();
            }
        });

        Label username = LabelFactory.create(USER_KEY);
        username.setStyleName(LABEL_STYLE);
        Label password = LabelFactory.create(PASSWORD_KEY);
        password.setStyleName(LABEL_STYLE);

        Grid grid = GridFactory.create(
                2, username, _username, password, _password);
        grid.setStyleName(GRID_STYLE);
        getLayout().add(grid);

        setFocus(_username);

        show();
    }

    /**
     * Invoked when the OK button is pressed.
     */
    @Override
    protected void onOK() {
        String username = _username.getText();
        String password = _password.getText();

        if (authenticate(username, password)) {
            close();
            OpenVPMSApp.getInstance().setContent(new ApplicationContentPane());
            _log.debug(username + " successfully logged in to OpenVPMS");
        } else {
            _log.debug(username + " attempted to log in to OpenVPMS but failed.");
        }
    }

    /**
     * Authenticate a user.
     *
     * @param username the user's nane
     * @param password the user's password
     * @return <code>true</code> if username and password are valid; otherwise
     *         <code>false</code>
     */
    protected boolean authenticate(String username, String password) {
/*
        if (username.compareToIgnoreCase("guest") != 0)
            return false;
        if (password.compareToIgnoreCase("stakeholder") != 0)
            return false;        
*/            
        return true;
    }

    /**
     * Helper to set the focus.
     *
     * @param component the component to focus on
     */
    protected void setFocus(Component component) {
        ApplicationInstance.getActive().setFocusedComponent(component);
    }

}
