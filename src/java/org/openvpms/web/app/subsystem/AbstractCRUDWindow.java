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
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectCreatorListener;
import org.openvpms.web.component.im.util.IMObjectDeletor;
import org.openvpms.web.component.im.util.IMObjectDeletorListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of the {@link CRUDWindow} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AbstractCRUDWindow<T extends IMObject> implements CRUDWindow<T> {

    /**
     * The object.
     */
    private T object;

    /**
     * Short names of archetypes that this may create.
     */
    private final ShortNames shortNames;

    /**
     * Localised type display name (e.g, Customer, Product).
     */
    private final String type;

    /**
     * The listener.
     */
    private CRUDWindowListener<T> listener;

    /**
     * The component representing this.
     */
    private Component component;

    /**
     * The action button row.
     */
    private ButtonRow buttons;

    /**
     * The edit button.
     */
    private Button edit;

    /**
     * The create button.
     */
    private Button create;

    /**
     * The delete button.
     */
    private Button delete;

    /**
     * The print button.
     */
    private Button print;

    /**
     * Edit button identifier.
     */
    private static final String EDIT_ID = "edit";

    /**
     * New button identifier.
     */
    private static final String NEW_ID = "new";

    /**
     * Delete button identifier.
     */
    private static final String DELETE_ID = "delete";

    /**
     * Print button identifier.
     */
    private static final String PRINT_ID = "print";


    /**
     * Constructs a new <code>AbstractCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public AbstractCRUDWindow(String type, ShortNames shortNames) {
        this.type = type;
        this.shortNames = shortNames;
    }

    /**
     * Sets the event listener.
     *
     * @param listener the event listener.
     */
    public void setListener(CRUDWindowListener<T> listener) {
        this.listener = listener;
    }

    /**
     * Returns the event listener.
     *
     * @return the event listener
     */
    public CRUDWindowListener<T> getListener() {
        return listener;
    }

    /**
     * Returns the component representing this.
     *
     * @return the component
     */
    public Component getComponent() {
        if (component == null) {
            component = doLayout();
        }
        return component;
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(T object) {
        this.object = object;
        GlobalContext.getInstance().setCurrent(object);
        getComponent();
        if (object != null) {
            enableButtons(buttons.getButtons(), true);
        } else {
            enableButtons(buttons.getButtons(), false);
        }
    }

    /**
     * Returns the object.
     *
     * @return the object, or <code>null</code> if there is none set
     */
    public T getObject() {
        return object;
    }

    /**
     * Returns the object's archetype descriptor.
     *
     * @return the object's archetype descriptor or <code>null</code> if there
     *         is no object set
     */
    public ArchetypeDescriptor getArchetypeDescriptor() {
        T object = getObject();
        ArchetypeDescriptor archetype = null;
        if (object != null) {
            archetype = DescriptorHelper.getArchetypeDescriptor(object);
        }
        return archetype;
    }

    /**
     * Invoked when the 'new' button is pressed.
     */
    public void onCreate() {
        onCreate(type, getShortNames());
    }

    /**
     * Returns display name for the types of objects that this may create.
     *
     * @return the display name for the types of objects that this may create
     */
    protected String getTypeDisplayName() {
        return type;
    }

    /**
     * Returns the short names of the archetypes that this may create.
     *
     * @return the short names
     */
    protected ShortNames getShortNames() {
        return shortNames;
    }

    /**
     * Lays out the component.
     */
    protected Component doLayout() {
        buttons = new ButtonRow("ControlRow");
        ButtonSet set = buttons.getButtons();
        layoutButtons(set);
        enableButtons(set, false);
        return buttons;
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
    }

    /**
     * Returns the edit button.
     *
     * @return the edit button
     */
    protected Button getEditButton() {
        if (edit == null) {
            edit = ButtonFactory.create(EDIT_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onEdit();
                }
            });
        }
        return edit;
    }

    /**
     * Returns the create button.
     *
     * @return the create button
     */
    protected Button getCreateButton() {
        if (create == null) {
            create = ButtonFactory.create(NEW_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onCreate();
                }
            });
        }
        return create;
    }

    /**
     * Returns the create button.
     *
     * @return the create button
     */
    protected Button getDeleteButton() {
        if (delete == null) {
            delete = ButtonFactory.create(DELETE_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onDelete();
                }
            });
        }
        return delete;
    }

    /**
     * Returns the print button.
     *
     * @return the print button
     */
    protected Button getPrintButton() {
        if (print == null) {
            print = ButtonFactory.create(PRINT_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onPrint();
                }
            });
        }
        return print;
    }

    /**
     * Returns the button set.
     *
     * @return the button set
     */
    protected ButtonSet getButtons() {
        return buttons.getButtons();
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        if (enable) {
            if (!buttons.contains(edit)) {
                buttons.add(edit);
            }
            if (!buttons.contains(delete)) {
                buttons.add(delete);
            }
        } else {
            buttons.remove(edit);
            buttons.remove(delete);
        }
    }

    /**
     * Invoked when the 'new' button is pressed.
     *
     * @param type       localised type display name
     * @param shortNames the short names
     */
    protected void onCreate(String type, ShortNames shortNames) {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated((T) object);
            }

            public void cancelled() {
                // ignore
            }
        };

        IMObjectCreator.create(type, shortNames.getShortNames(), listener);
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param object the new object
     */
    protected void onCreated(T object) {
        edit(object);
    }

    /**
     * Invoked when the edit button is pressed. This popups up an {@link
     * EditDialog}.
     */
    protected void onEdit() {
        T object = getObject();
        if (object != null) {
            if (object.isNew()) {
                edit(object);
            } else {
                // make sure the latest instance is being used.
                object = IMObjectHelper.reload(object);
                if (object == null) {
                    ErrorDialog.show(Messages.get("imobject.noexist"), type);
                } else {
                    edit(object);
                }
            }
        }
    }

    /**
     * Invoked when the delete button is pressed.
     */
    @SuppressWarnings("unchecked")
    protected void onDelete() {
        T object = IMObjectHelper.reload(getObject());
        if (object == null) {
            ErrorDialog.show(Messages.get("imobject.noexist"), type);
        } else {
            IMObjectDeletor.delete(object, new IMObjectDeletorListener<T>() {
                public void deleted(T object) {
                    onDeleted(object);
                }

                public void deactivated(T object) {
                    onSaved(object, false);
                }

                public void cancelled() {
                }
            });
        }
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    protected void onPrint() {
        T object = getObject();
        try {
            IMPrinter<T> printer = createPrinter(object);
            printer.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit.
     * @param context the layout context
     * @return a new editor
     */
    protected IMObjectEditor createEditor(T object,
                                          LayoutContext context) {
        return IMObjectEditorFactory.create(object, context);
    }

    /**
     * Creates a new edit dialog.
     *
     * @param editor the editor
     */
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new EditDialog(editor);
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param isNew  determines if the object is a new instance
     */
    protected void onEditCompleted(IMObjectEditor editor, boolean isNew) {
        if (editor.isDeleted()) {
            onDeleted((T) editor.getObject());
        } else if (editor.isSaved()) {
            onSaved((T) editor.getObject(), isNew);
        } else {
            GlobalContext.getInstance().setCurrent(null);
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(T object, boolean isNew) {
        setObject(object);
        if (listener != null) {
            listener.saved(object, isNew);
        }
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(T object) {
        setObject(null);
        if (listener != null) {
            listener.deleted(object);
        }
    }

    /**
     * Creates a new printer.
     *
     * @param object the object to print
     * @return an instance of {@link InteractiveIMPrinter}. This overrides
     *         the default interactive behaviour so that printing is always
     *         interactive
     * @throws OpenVPMSException for any error
     */
    protected IMPrinter<T> createPrinter(T object) {
        IMPrinter<T> printer = IMPrinterFactory.create(object);
        InteractiveIMPrinter<T> iPrinter = new InteractiveIMPrinter<T>(printer);
        iPrinter.setInteractive(true);
        return iPrinter;
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(T object) {
        setObject(null);
        if (listener != null) {
            listener.refresh(object);
        }
    }

    /**
     * Creates a layout context for editing an object.
     *
     * @return a new layout context.
     */
    protected LayoutContext createLayoutContext() {
        return new DefaultLayoutContext(true);
    }

    /**
     * Edit an object.
     *
     * @param object the object to edit
     */
    private void edit(T object) {
        try {
            final boolean isNew = object.isNew();

            LayoutContext context = createLayoutContext();
            final IMObjectEditor editor = createEditor(object, context);
            EditDialog dialog = createEditDialog(editor);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    onEditCompleted(editor, isNew);
                }
            });

            GlobalContext.getInstance().setCurrent(object);
            dialog.show();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
