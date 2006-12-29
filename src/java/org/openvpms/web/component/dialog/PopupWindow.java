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

package org.openvpms.web.component.dialog;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Styles;


/**
 * Generic popup window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class PopupWindow extends WindowPane {

    /**
     * The layout pane.
     */
    private final SplitPane layout;

    /**
     * The button row.
     */
    private final ButtonRow row;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup;

    /**
     * The default button.
     */
    private String defaultButton;


    /**
     * Construct a new <code>PopupWindow</code>.
     *
     * @param title the window title
     */
    public PopupWindow(String title) {
        this(title, null, null);
    }

    /**
     * Construct a new <code>PopupWindow</code>
     *
     * @param title the window title. May be <code>null</code>
     * @param style the window style. May be <code>null</code>
     * @param focus the focus group. May be <code>null</code>
     */
    public PopupWindow(String title, String style, FocusGroup focus) {
        super(title, null, null);
        if (style == null) {
            style = "PopupWindow";
        }
        style = Styles.getStyle(WindowPane.class, style);
        setStyleName(style);
        focusGroup = new FocusGroup(getClass().getName());
        if (focus != null) {
            focusGroup.add(focus);
        }

        row = new ButtonRow(focusGroup);

        layout = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "PopupWindow.Layout");
        layout.add(row);
        add(layout);
    }

    /**
     * Show the window.
     */
    public void show() {
        if (getParent() == null) {
            DialogManager.show(this);
        }
        if (defaultButton != null) {
            Button button = getButtons().getButton(defaultButton);
            if (button != null) {
                ApplicationInstance.getActive().setFocusedComponent(button);
            }
        }
    }

    /**
     * Sets the default button identifier.
     *
     * @param id the button identifier
     */
    public void setDefaultButton(String id) {
        defaultButton = id;
    }

    /**
     * Returns the default button identifier.
     *
     * @return the default button, or <code>null</code> if none has been set
     */
    public String getDefaultButton() {
        return defaultButton;
    }

    /**
     * Close the window.
     */
    public void close() {
        userClose();
    }

    /**
     * Returns the layout pane.
     */
    protected SplitPane getLayout() {
        return layout;
    }

    /**
     * Returns the buttons.
     *
     * @return the buttons
     */
    protected ButtonSet getButtons() {
        return row.getButtons();
    }

    /**
     * Adds a button.
     *
     * @param id       the button identifier
     * @param listener the action listener
     * @return a new button
     */
    protected Button addButton(String id, ActionListener listener) {
        return addButton(id, listener, false);
    }

    /**
     * Adds a button.
     *
     * @param id              the button identifier
     * @param listener        the action listener
     * @param disableShortcut if <code>true</code> disable any keyboard shortcut
     * @return a new button
     */
    protected Button addButton(String id, ActionListener listener,
                               boolean disableShortcut) {
        return row.addButton(id, listener, disableShortcut);
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    protected FocusGroup getFocusGroup() {
        return focusGroup;
    }

}
