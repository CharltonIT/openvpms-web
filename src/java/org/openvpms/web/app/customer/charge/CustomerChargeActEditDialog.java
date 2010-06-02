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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.customer.charge;

import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.component.print.BatchPrintDialog;
import org.openvpms.web.component.print.BatchPrinter;
import org.openvpms.web.component.print.PrintHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.List;


/**
 * An edit dialog for {@link CustomerChargeActEditor} editors.
 * <p/>
 * This performs printing of unprinted documents that have their <em>interactive</em> flag set to <tt>true</tt>
 * when <em>Apply</em> or <em>OK</em> is pressed.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerChargeActEditDialog extends ActEditDialog {

    /**
     * Constructs a <tt>CustomerChargeActEditDialog</tt>.
     *
     * @param editor the editor
     */
    public CustomerChargeActEditDialog(CustomerChargeActEditor editor) {
        super(editor);
    }

    /**
     * Saves the current object, and prints any interactive documents associated with the charge.
     */
    @Override
    public void onOK() {
        if (save()) {
            List<IMObject> docs = getDocumentsToPrint();
            if (!docs.isEmpty()) {
                printDocuments(docs, true);
            } else {
                close(OK_ID);
            }
        }
    }

    /**
     * Saves the current object, and prints any interactive documents associated with the charge.
     */
    @Override
    public void onApply() {
        if (save()) {
            List<IMObject> docs = getDocumentsToPrint();
            if (!docs.isEmpty()) {
                printDocuments(docs, false);
            }
        }
    }

    /**
     * Prints any unprinted documents flagged as <em>interactive</em>, associated with the charge.
     *
     * @param documents  the documents to print
     * @param close if <tt>true</tt>, close the edit dialog on completion
     */
    private void printDocuments(List<IMObject> documents, final boolean close) {
        String title = Messages.get("workflow.checkout.print.title");
        final BatchPrintDialog dialog = new BatchPrintDialog(title, documents);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                String action = dialog.getAction();
                if (BatchPrintDialog.OK_ID.equals(action)) {
                    List<IMObject> docs = dialog.getSelected();
                    if (!docs.isEmpty()) {
                        printSelected(docs, close);
                    } else if (close) {
                        close(OK_ID);
                    }
                } else if (close) {
                    close(OK_ID);
                }
            }
        });
        dialog.show();
    }

    /**
     * Returns a list of documents to print. These are all acts associated with the <em>documents</em> node that are:
     * <ul>
     * <li>unprinted; and
     * <li>have a document template that is flagged <em>interactive</em>
     * </ul>
     *
     * @return a list of documents to print
     */
    private List<IMObject> getDocumentsToPrint() {
        List<IMObject> acts = new ArrayList<IMObject>();
        Act act = (Act) getEditor().getObject();
        ActBean bean = new ActBean(act);
        Context context = GlobalContext.getInstance();
        for (Act item : bean.getNodeActs("items")) {
            ActBean itemBean = new ActBean(item);
            for (Act document : itemBean.getNodeActs("documents")) {
                ActBean documentBean = new ActBean(document);
                if (!documentBean.getBoolean("printed") && documentBean.hasNode("documentTemplate")) {
                    Entity template = documentBean.getNodeParticipant("documentTemplate");
                    if (template != null) {
                        EntityRelationship rel = PrintHelper.getDocumentTemplatePrinter(template, context);
                        if (rel != null) {
                            IMObjectBean relBean = new IMObjectBean(rel);
                            if (relBean.getBoolean("interactive")) {
                                acts.add(document);
                            }
                        }
                    }
                }
            }
        }
        return acts;
    }

    /**
     * Prints selected documents.
     *
     * @param documents the documents to print
     * @param close     if <tt>true</tt>, closes the dialog on completion
     */
    private void printSelected(List<IMObject> documents, final boolean close) {
        BatchPrinter printer = new BatchPrinter(documents) {
            public void cancelled() {
                completed();
            }

            public void skipped() {
                print(); // print the next document
            }

            public void failed(Throwable cause) {
                ErrorHelper.show(cause, new WindowPaneListener() {
                    public void onClose(WindowPaneEvent event) {
                        print(); // print the next document
                    }
                });
            }

            /**
             * Invoked when printing completes. Closes the edit dialog if required.
             */
            @Override
            protected void completed() {
                if (close) {
                    close(OK_ID);
                }
            }
        };
        printer.print();
    }

}