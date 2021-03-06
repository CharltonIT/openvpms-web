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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;


/**
 * Displays a {@link Browser} in a popup dialog.
 *
 * @author Tim Anderson
 */
public class BrowserDialog<T> extends PopupDialog {

    /**
     * New button identifier.
     */
    public static final String NEW_ID = "new";

    /**
     * The selected object.
     */
    private T selected;

    /**
     * Determines if the user wants to create a new object. Set when the 'New'
     * button is pressed.
     */
    private boolean createNew = false;

    /**
     * The browser.
     */
    private Browser<T> browser;

    /**
     * Determines if the dialog should close on selection.
     */
    private boolean closeOnSelection = true;

    /**
     * Window style name.
     */
    private static final String STYLE = "BrowserDialog";


    /**
     * Constructs a {@code BrowserDialog}.
     *
     * @param title   the dialog title
     * @param browser the browser
     * @param help    the help context
     */
    public BrowserDialog(String title, Browser<T> browser, HelpContext help) {
        this(title, browser, false, help);
    }

    /**
     * Constructs a {@code BrowserDialog}.
     *
     * @param title   the dialog title
     * @param buttons the buttons to display
     * @param browser the browser
     * @param help    the help context
     */
    public BrowserDialog(String title, String[] buttons, Browser<T> browser, HelpContext help) {
        this(title, null, buttons, browser, false, help);
    }

    /**
     * Constructs a {@code BrowserDialog}.
     *
     * @param title   the dialog title
     * @param browser the browser
     * @param addNew  if {@code true} add a 'new' button
     * @param help    the help context
     */
    public BrowserDialog(String title, Browser<T> browser, boolean addNew, HelpContext help) {
        this(title, null, CANCEL, browser, addNew, help);
    }

    /**
     * Constructs a {@code BrowserDialog}.
     *
     * @param title   the dialog title
     * @param message the dialog message. May be {@code null}
     * @param buttons the buttons to display
     * @param browser the browser
     * @param addNew  if {@code true} add a 'new' button
     * @param help    the help context
     */
    public BrowserDialog(String title, String message, String[] buttons,
                         Browser<T> browser, boolean addNew, HelpContext help) {
        this(title, buttons, addNew, help);
        init(browser, message);
    }

    /**
     * Constructs a {@link BrowserDialog}.
     * <p/>
     * Subclasses may use this constructor to lazily initialise the browser. They can invoke {@link #init} to
     * initialise it after construction.
     *
     * @param title   the dialog title
     * @param buttons the buttons to display
     * @param addNew  if {@code true} add a 'new' button
     * @param help    the help context
     */
    protected BrowserDialog(String title, String[] buttons, boolean addNew, HelpContext help) {
        super(title, STYLE, buttons, help);
        setModal(true);

        if (addNew) {
            addButton(NEW_ID, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onNew();
                }
            });
        }
    }

    /**
     * Determines if the dialog should close on selection.
     * <p/>
     * Defaults to {@code true}.
     *
     * @param close if {@code true}, close the dialog when an object is selected
     */
    public void setCloseOnSelection(boolean close) {
        closeOnSelection = close;
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    public Browser<T> getBrowser() {
        return browser;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or {@code null} if none was selected
     */
    public T getSelected() {
        return selected;
    }

    /**
     * Determines if an object has been selected.
     *
     * @return {@code true} if an object has been selected, otherwise {@code false}
     */
    public boolean isSelected() {
        return getSelected() != null;
    }

    /**
     * Determines if the 'New' button was selected, indicating that a new object
     * should be created.
     *
     * @return {@code true} if 'New' was selected
     */
    public boolean createNew() {
        return createNew;
    }

    /**
     * Initialise the dialog.
     * <p/>
     * This method may only be invoked once.
     *
     * @param browser the browser
     * @param message the dialog message. May be {@code null}
     */
    protected void init(Browser<T> browser, String message) {
        Component component = browser.getComponent();
        if (message != null) {
            Label label = LabelFactory.create(null, Styles.BOLD);
            label.setText(message);
            Row inset = RowFactory.create(Styles.INSET, label);
            Column column = ColumnFactory.create(Styles.CELL_SPACING, inset, component);
            getLayout().add(column);
        } else {
            getLayout().add(component);
        }

        browser.addBrowserListener(new BrowserListener<T>() {
            public void query() {
            }

            public void selected(T object) {
                onSelected(object);
            }

            public void browsed(T object) {
                onBrowsed(object);
            }
        });
        getFocusGroup().add(0, browser.getFocusGroup());
        this.browser = browser;
    }

    /**
     * Invoked when the 'OK' button is pressed. This closes the dialog if an object is selected.
     */
    @Override
    protected void onOK() {
        if (isSelected()) {
            super.onOK();
        }
    }

    /**
     * Sets the action and closes the window.
     * <p/>
     * If the action isn't {@link #CANCEL_ID}, the browser's state will be saved.
     * <p/>
     * If the action is {@link #CANCEL_ID} any selection will be discarded.
     *
     * @param action the action
     */
    @Override
    protected void close(String action) {
        if (CANCEL_ID.equals(action)) {
            setSelected(null);
        } else {
            BrowserStates.getInstance().add(browser);
        }
        super.close(action);
    }

    /**
     * Sets the selected object.
     *
     * @param object the selected object. May be {@code null}
     */
    protected void setSelected(T object) {
        selected = object;
    }

    /**
     * Select the current object using {@link #setSelected}, and if {@code closeOnSelection} is {@code true},
     * invokes {@link #onOK} to close the browser.
     *
     * @param object the selected object
     */
    protected void onSelected(T object) {
        setSelected(object);
        if (closeOnSelection) {
            onOK();
        }
    }

    /**
     * Updates the current selection using {@link #setSelected} , but doesn't close the browser.
     *
     * @param object the selected object
     */
    protected void onBrowsed(T object) {
        setSelected(object);
    }

    /**
     * Flags that the user wants to create a new instance, and closes the
     * browser.
     */
    protected void onNew() {
        createNew = true;
        close(NEW_ID);
    }

}
