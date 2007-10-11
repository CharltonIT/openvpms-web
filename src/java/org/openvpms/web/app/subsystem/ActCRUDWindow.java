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

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.servlet.DownloadServlet;


/**
 * CRUD Window for acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ActCRUDWindow<T extends Act>
        extends AbstractViewCRUDWindow<T> {

    /**
     * The 'post' button.
     */
    private Button post;

    /**
     * Post button identifier.
     */
    private static final String POST_ID = "post";

    /**
     * The 'preview' button.
     */
    private Button preview;

    /**
     * Preview button identifier.
     */
    private static final String PREVIEW_ID = "preview";


    /**
     * Create a new <code>ActCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create
     */
    public ActCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act
     * @return <code>true</code> if the act can be edited, otherwise
     *         <code>false</code>
     */
    protected boolean canEdit(Act act) {
        String status = act.getStatus();
        return !POSTED.equals(status);
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act
     * @return <code>true</code> if the act can be deleted, otherwise
     *         <code>false</code>
     */
    protected boolean canDelete(Act act) {
        String status = act.getStatus();
        return !POSTED.equals(status);
    }

    /**
     * Invoked when the edit button is pressed. This popups up an {@link
     * EditDialog}.
     */
    @Override
    protected void onEdit() {
        Act act = getObject();
        if (act != null) {
            if (canEdit(act)) {
                super.onEdit();
            } else {
                showStatusError(act, "act.noedit.title", "act.noedit.message");
            }
        }
    }

    /**
     * Invoked when the delete button is pressed.
     */
    @Override
    protected void onDelete() {
        Act act = getObject();
        if (canDelete(act)) {
            super.onDelete();
        } else {
            showStatusError(act, "act.nodelete.title", "act.nodelete.message");
        }
    }

    /**
     * Invoked when the 'post' button is pressed.
     */
    protected void onPost() {
        try {
            final T act = getObject();
            final IMPrinter<T> printer = IMPrinterFactory.create(act);
            final PostDialog dialog = new PostDialog(
                    Messages.get("act.post.title", getTypeDisplayName()));
            dialog.setDefaultPrinter(printer.getDefaultPrinter());
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent e) {
                    if (PostDialog.OK_ID.equals(dialog.getAction())) {
                        try {
                            boolean saved = post(act);
                            if (dialog.print()) {
                                try {
                                    printer.print(dialog.getPrinter());
                                    saved |= printed(act);
                                } catch (OpenVPMSException exception) {
                                    ErrorHelper.show(exception);
                                }
                            }
                            if (saved) {
                                // act was saved. Need to refresh
                                saved(act);
                            }
                        } catch (OpenVPMSException exception) {
                            ErrorHelper.show(exception);
                        }
                    }
                }
            });
            dialog.show();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the 'preview' button is pressed.
     */
    protected void onPreview() {
        try {
            final IMPrinter<T> printer
                    = IMPrinterFactory.create(getObject());
            Document document = printer.getDocument();
            DownloadServlet.startDownload(document);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Creates a new printer.
     *
     * @param object the object to print
     * @return an instance of {@link InteractiveIMPrinter}.
     * @throws OpenVPMSException for any error
     */
    @Override
    protected IMPrinter<T> createPrinter(final T object) {
        InteractiveIMPrinter<T> printer
                = (InteractiveIMPrinter<T>) super.createPrinter(object);
        printer.setListener(new PrinterListener() {
            public void printed(String printer) {
                if (ActCRUDWindow.this.printed(object)) {
                    saved(object);
                }
            }

            public void cancelled() {
            }

            public void skipped() {
            }

            public void failed(Throwable cause) {
                ErrorHelper.show(cause);
            }
        });
        return printer;
    }

    /**
     * Invoked when an act has been successfully printed. Updates the
     * act's printed flag if necessary, and saves the act.
     *
     * @param object the object
     * @return <tt>true</tt> if the act was saved
     */
    protected boolean printed(T object) {
        boolean saved = false;
        try {
            if (setPrintStatus(object, true)) {
                saved = SaveHelper.save(object);
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
        return saved;
    }

    /**
     * Sets the print status.
     *
     * @param act     the act
     * @param printed the print status
     * @return <code>true</code> if the print status was changed,
     *         <code>false</code> if the act doesn't have a 'printed' node or
     *         its value is the same as that supplied
     */
    protected boolean setPrintStatus(Act act, boolean printed) {
        ActBean bean = new ActBean(act);
        if (bean.hasNode("printed") && bean.getBoolean("printed") != printed) {
            bean.setValue("printed", printed);
            return true;
        }
        return false;
    }

    /**
     * Helper to show a status error.
     *
     * @param act        the act
     * @param titleKey   the error dialog title key
     * @param messageKey the error messsage key
     */
    protected void showStatusError(Act act, String titleKey,
                                   String messageKey) {
        String name = getArchetypeDescriptor().getDisplayName();
        String status = act.getStatus();
        String title = Messages.get(titleKey, name);
        String message = Messages.get(messageKey, name, status);
        ErrorDialog.show(title, message);
    }

    /**
     * Returns the post button.
     *
     * @return the post button
     */
    protected Button getPostButton() {
        if (post == null) {
            post = ButtonFactory.create(POST_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onPost();
                }
            });
        }
        return post;
    }

    /**
     * Returns the preview button.
     *
     * @return the preview button
     */
    protected Button getPreviewButton() {
        if (preview == null) {
            preview = ButtonFactory.create(PREVIEW_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onPreview();
                }
            });
        }
        return preview;
    }

    /**
     * Posts an act. This changes the act's status to POSTED, and saves it.
     *
     * @param act the act to post
     * @return <tt>true</tt> if the act was saved
     */
    private boolean post(Act act) {
        String status = act.getStatus();
        if (!POSTED.equals(status)) {
            act.setStatus(POSTED);
            return SaveHelper.save(act);
        }
        return false;
    }

    /**
     * Invoked when an act is saved. Refreshes the window and notifies any
     * registered listener.
     *
     * @param act the act
     */
    private void saved(T act) {
        setObject(act);
        CRUDWindowListener<T> listener = getListener();
        if (listener != null) {
            listener.saved(act, false);
        }
    }

}
