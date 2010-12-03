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
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.event.WindowPaneListener;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.util.AbstractIMObjectDeletionListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectCreatorListener;
import org.openvpms.web.component.im.util.IMObjectDeletor;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of the {@link CRUDWindow} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractCRUDWindow<T extends IMObject>
        implements CRUDWindow<T> {

    /**
     * The object.
     */
    private T object;

    /**
     * The archetypes that this may create.
     */
    private final Archetypes<T> archetypes;

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
     * Edit button identifier.
     */
    protected static final String EDIT_ID = "edit";

    /**
     * New button identifier.
     */
    protected static final String NEW_ID = "new";

    /**
     * Delete button identifier.
     */
    protected static final String DELETE_ID = "delete";

    /**
     * Print button identifier.
     */
    protected static final String PRINT_ID = "print";


    /**
     * Constructs a new <tt>AbstractCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public AbstractCRUDWindow(Archetypes<T> archetypes) {
        this.archetypes = archetypes;
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
     * @param object the object. May be <tt>null</tt>
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
     * @return the object, or <tt>null</tt> if there is none set
     */
    public T getObject() {
        return object;
    }

    /**
     * Returns the object's archetype descriptor.
     *
     * @return the object's archetype descriptor or <tt>null</tt> if there
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
     * Creates and edits a new object.
     */
    public void create() {
        onCreate(getArchetypes());
    }

    /**
     * Edits the current object.
     */
    public void edit() {
        T object = getObject();
        if (object != null) {
            if (object.isNew()) {
                edit(object);
            } else {
                // make sure the latest instance is being used.
                object = IMObjectHelper.reload(object);
                if (object == null) {
                    ErrorDialog.show(Messages.get("imobject.noexist", archetypes.getDisplayName()));
                } else {
                    edit(object);
                }
            }
        }
    }

    /**
     * Returns the archetypes that this may create.
     *
     * @return the archetypes
     */
    protected Archetypes<T> getArchetypes() {
        return archetypes;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    protected Component doLayout() {
        buttons = new ButtonRow("ControlRow");
        ButtonSet set = buttons.getButtons();
        set.setHideDisabled(true);
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
        buttons.add(createNewButton());
        buttons.add(createEditButton());
        buttons.add(createDeleteButton());
    }

    /**
     * Helper to create a new button with id {@link #EDIT_ID} linked to {@link #edit()}.
     *
     * @return a new button
     */
    protected Button createEditButton() {
        return ButtonFactory.create(EDIT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                edit();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #NEW_ID} linked to {@link #create()}.
     *
     * @return a new button
     */
    protected Button createNewButton() {
        return ButtonFactory.create(NEW_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                create();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #DELETE_ID} linked to {@link #onDelete()}.
     *
     * @return a new button
     */
    protected Button createDeleteButton() {
        return ButtonFactory.create(DELETE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onDelete();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #PRINT_ID} linked to {@link #onPrint()}.
     *
     * @return a new button
     */
    protected Button createPrintButton() {
        return ButtonFactory.create(PRINT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onPrint();
            }
        });
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
        buttons.setEnabled(EDIT_ID, enable);
        buttons.setEnabled(DELETE_ID, enable);
    }

    /**
     * Invoked when the 'new' button is pressed.
     *
     * @param archetypes the archetypes
     */
    @SuppressWarnings("unchecked")
    protected void onCreate(Archetypes<T> archetypes) {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated((T) object);
            }

            public void cancelled() {
                // ignore
            }
        };

        IMObjectCreator.create(archetypes, listener);
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
     * Edits an object.
     *
     * @param object the object to edit
     */
    protected void edit(T object) {
        try {
            LayoutContext context = createLayoutContext();
            IMObjectEditor editor = createEditor(object, context);
            edit(editor);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Edits an object.
     *
     * @param editor the object editor
     * @return the edit dialog
     */
    @SuppressWarnings("unchecked")
    protected EditDialog edit(final IMObjectEditor editor) {
        T object = (T) editor.getObject();
        final boolean isNew = object.isNew();
        EditDialog dialog = createEditDialog(editor);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                onEditCompleted(editor, isNew);
            }
        });
        GlobalContext.getInstance().setCurrent(object);
        dialog.show();
        return dialog;
    }

    /**
     * Invoked when the delete button is pressed.
     */
    @SuppressWarnings("unchecked")
    protected void onDelete() {
        T object = IMObjectHelper.reload(getObject());
        if (object == null) {
            ErrorDialog.show(Messages.get("imobject.noexist", archetypes.getDisplayName()));
        } else {
            IMObjectDeletor.delete(object, new AbstractIMObjectDeletionListener<T>() {
                public void deleted(T object) {
                    onDeleted(object);
                }

                public void deactivated(T object) {
                    onSaved(object, false);
                }
            });
        }
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    protected void onPrint() {
        print(getObject());
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
     * <p/>
     * This implementation uses {@link EditDialogFactory#create}.
     *
     * @param editor the editor
     * @return a new edit dialog
     */
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return EditDialogFactory.create(editor);
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param isNew  determines if the object is a new instance
     */
    @SuppressWarnings("unchecked")
    protected void onEditCompleted(IMObjectEditor editor, boolean isNew) {
        if (editor.isDeleted()) {
            onDeleted((T) editor.getObject());
        } else if (editor.isSaved()) {
            onSaved((T) editor.getObject(), isNew);
        } else {
            // cancelled
            onRefresh((T) editor.getObject());
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
     * @return an instance of {@link InteractiveIMPrinter}.
     * @throws OpenVPMSException for any error
     */
    protected IMPrinter<T> createPrinter(T object) {
        IMPrinter<T> printer = IMPrinterFactory.create(object);
        return new InteractiveIMPrinter<T>(printer);
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
     * Print an object.
     *
     * @param object the object to print
     */
    protected void print(T object) {
        try {
            IMPrinter<T> printer = createPrinter(object);
            printer.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }
}
