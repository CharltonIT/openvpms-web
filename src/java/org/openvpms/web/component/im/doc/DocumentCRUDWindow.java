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

package org.openvpms.web.component.im.doc;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.print.IMObjectPrinter;
import org.openvpms.web.component.im.print.IMObjectPrinterListener;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Document CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DocumentCRUDWindow extends ActCRUDWindow {


    /**
     * The object used to generate the document 
     */
    private final IMObject _genobject;

    /**
     * The refresh button.
     */
    private Button _refresh;

    /**
     * Refresh button identifier.
     */
    private static final String REFRESH_ID = "refresh";


    /**
     * Create a new <code>DocumentCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create
     */
    public DocumentCRUDWindow(String type, String[] shortNames, IMObject object) {
        super(type, new ShortNameList(shortNames));
        _genobject = object;
    }

    /**
     * Invoked when an object has been successfully printed.
     *
     * @param object the object
     */
    @Override
    protected void printed(IMObject object) {
        Act act = (Act) object;
        try {
            setPrintStatus(act, true);
            SaveHelper.save(act);
            setObject(act);
            CRUDWindowListener listener = getListener();
            if (listener != null) {
                listener.saved(act, false);
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }


    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        _refresh = ButtonFactory.create(REFRESH_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onRefresh();
            }
        });

        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
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
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            buttons.add(getPrintButton());
            if (canRefresh()) {
                buttons.add(_refresh);
            }
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Creates a new printer.
     *
     * @return a new printer.
     */
    @Override
    protected IMObjectPrinter createPrinter() {
        String type = getTypeDisplayName();
        IMObjectPrinter printer = new DocumentActPrinter(type, Context.getInstance().getPatient());
        printer.setListener(new IMObjectPrinterListener() {
            public void printed(IMObject object) {
                DocumentCRUDWindow.this.printed(object);
            }
        });
        return printer;
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    @Override
    protected void onPrint() {
        boolean print = true;
        DocumentAct act = (DocumentAct) getObject();
        if (act.getDocReference() == null) {
            if (canRefresh()) {
                if (!refresh()) {
                    print = false;
                }
            }
        }
        if (print) {
            super.onPrint();
        }
    }

    /**
     * Invoked when the 'refresh' button is pressed.
     */
    private void onRefresh() {
        String title = Messages.get("patient.document.refresh.title");
        String message = Messages.get("patient.document.refresh.message");
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    refresh();
                }
            }
        });
        dialog.show();
    }

    /**
     * Refreshes the current document act.
     *
     * @return <code>true</code> if the document was refreshed
     */
    private boolean refresh() {
        boolean refreshed = false;
        DocumentAct act = (DocumentAct) getObject();
        DocumentActEditor editor
                = new DocumentActEditor(act, null, null, _genobject);
        if (editor.refresh()) {
            refreshed = editor.save();
            onSaved(act, false);
        }
        return refreshed;
    }

    /**
     * Determines if a document can be refreshed.
     *
     * @return <code>true</code> if the document can be refreshed, otherwise
     *         <code>false</code>
     */
    private boolean canRefresh() {
        ActBean act = new ActBean((Act) getObject());
        return act.hasNode("documentTemplate") && act.hasNode("docReference");
    }

}
