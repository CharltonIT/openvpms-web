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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusCommand;
import org.openvpms.web.component.im.edit.EditResultSetDialog;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.view.ViewResultSetDialog;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * A <tt>CRUDWindow</tt> that supports iteration over a {@link ResultSet}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ResultSetCRUDWindow<T extends IMObject> extends AbstractCRUDWindow<T> {

    /**
     * The view button identifier.
     */
    protected static final String VIEW_ID = "view";

    /**
     * A result set to iterate over.
     */
    private ResultSet<T> set;


    /**
     * Constructs a <tt>ResultSetCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create instances of
     * @param set        the result set. May be <tt>null</tt>
     */
    public ResultSetCRUDWindow(Archetypes<T> archetypes, ResultSet<T> set) {
        super(archetypes);
        setResultSet(set);
    }

    /**
     * Sets the result set.
     *
     * @param set the result set
     */
    public void setResultSet(ResultSet<T> set) {
        this.set = set;
        boolean enable = getObject() != null && set != null;
        createViewButton().setEnabled(enable);
    }

    /**
     * Returns the result set.
     *
     * @return the result set. May be <tt>null</tt>
     */
    public ResultSet<T> getResultSet() {
        return set;
    }

    /**
     * Views the selected object.
     */
    public void view() {
        final T object = getObject();
        if (object != null) {
            final FocusCommand focus = new FocusCommand();
            String title = DescriptorHelper.getDisplayName(object);
            final ViewResultSetDialog<T> dialog = new ViewResultSetDialog<T>(title, object, set);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                protected void onAction(PopupDialog dialog) {
                    focus.restore();
                    super.onAction(dialog);
                }

                @Override
                public void onAction(String action) {
                    if (ViewResultSetDialog.EDIT_ID.equals(action)) {
                        T selected = dialog.getSelected();
                        if (selected != null) {
                            edit(selected);
                        }
                    }
                }
            });
            dialog.show();
        }
    }

    /**
     * Edits an object.
     *
     * @param object the object to edit
     */
    @Override
    protected void edit(final T object) {
        if (object.isNew()) {
            super.edit(object);
        } else {
            final FocusCommand focus = new FocusCommand();
            String title = Messages.get("editor.edit.title", getArchetypes().getDisplayName());
            EditResultSetDialog<T> dialog = new EditResultSetDialog<T>(title, object, set);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                protected void onAction(PopupDialog dialog) {
                    focus.restore();
                    super.onAction(dialog);
                }
            });
            dialog.show();
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createViewButton(), 1); // add after new, before edit
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        boolean enableViewEdit = enable && set != null;
        buttons.setEnabled(VIEW_ID, enableViewEdit);
        buttons.setEnabled(EDIT_ID, enableViewEdit);
        buttons.setEnabled(DELETE_ID, enable);
    }

    /**
     * Helper to create a new button with id {@link #VIEW_ID} linked to {@link #view}.
     *
     * @return a new view button
     */
    protected Button createViewButton() {
        return ButtonFactory.create(VIEW_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                view();
            }
        });
    }
}