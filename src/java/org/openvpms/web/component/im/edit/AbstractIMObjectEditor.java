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

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.edit.Validator;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.view.AbstractIMObjectView;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.IMObjectView;
import org.openvpms.web.resource.util.Messages;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link IMObjectEditor} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectEditor
        implements IMObjectEditor {

    /**
     * The object being edited.
     */
    private final IMObject object;

    /**
     * The parent object. May be <code>null</code>.
     */
    private final IMObject parent;

    /**
     * The object's descriptor.
     */
    private final ArchetypeDescriptor archetype;

    /**
     * The object viewer.
     */
    private IMObjectView viewer;

    /**
     * The child editors.
     */
    private Editors editors = new Editors();

    /**
     * The object properties.
     */
    private PropertySet properties;

    /**
     * Lookup fields. These may beed to be refreshed.
     */
    private List<SelectField> lookups = new ArrayList<SelectField>();

    /**
     * The layout context.
     */
    private LayoutContext context;

    /**
     * Action listener for layout changes.
     */
    private ActionListener layoutChangeListener;

    /**
     * Indicates if the object has been saved.
     */
    private boolean saved = false;

    /**
     * Indicates if the object was deleted.
     */
    private boolean deleted = false;

    /**
     * Indicates if editing was cancelled.
     */
    private boolean cancelled = false;

    /**
     * Property change listener notifier.
     */
    private PropertyChangeSupport propertyChangeNotifier;

    /**
     * Listener to update derived fields.
     */
    private ModifiableListener derivedFieldRefresher;


    /**
     * Construct a new <code>AbstractIMObjectEditor</code>.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be <code>null</code>
     * @param layoutContext the layout context. May be <code>null</code>.
     */
    public AbstractIMObjectEditor(IMObject object, IMObject parent,
                                  LayoutContext layoutContext) {
        this.object = object;
        this.parent = parent;

        if (layoutContext == null) {
            context = new DefaultLayoutContext(true);
        } else {
            context = new DefaultLayoutContext(layoutContext);
        }
        // establish a local context if one not already present
        if (context.getContext() == GlobalContext.getInstance()) {
            context.setContext(new LocalContext());
        }

        archetype = DescriptorHelper.getArchetypeDescriptor(object);
        properties = new PropertySet(object, archetype);
        IMObjectComponentFactory factory = new ComponentFactory(context);
        context.setComponentFactory(factory);

        derivedFieldRefresher = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateDerivedFields(modifiable);
            }
        };
        editors.addModifiableListener(derivedFieldRefresher);
    }

    /**
     * Returns a title for the editor.
     *
     * @return a title for the editor
     */
    public String getTitle() {
        String title;
        if (object.isNew()) {
            title = Messages.get("editor.new.title", getDisplayName());
        } else {
            title = Messages.get("editor.edit.title", getDisplayName());
        }
        return title;
    }

    /**
     * Returns a display name for the object being edited.
     *
     * @return a display name for the object
     */
    public String getDisplayName() {
        return getArchetypeDescriptor().getDisplayName();
    }

    /**
     * Returns the object being edited.
     *
     * @return the object being edited
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object. May be <code>null</code>
     */
    public IMObject getParent() {
        return parent;
    }

    /**
     * Returns the archetype descriptor of the object.
     *
     * @return the object's archetype descriptor
     */
    public ArchetypeDescriptor getArchetypeDescriptor() {
        return archetype;
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    public boolean save() {
        if (cancelled) {
            return false;
        }
        boolean result = false;
        Validator validator = new Validator();
        if (validator.validate(this)) {
            if (!isModified()) {
                result = true;
            } else {
                result = doSave();
                if (result) {
                    saved = true;
                    clearModified();
                }
            }
        } else {
            ValidationHelper.showError(validator);
        }
        return result;
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return <code>true</code> if edits have been saved.
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Delete the current object.
     *
     * @return <code>true</code> if the object was deleted successfully
     */
    public boolean delete() {
        if (cancelled) {
            return false;
        }
        boolean result = false;
        IMObject object = getObject();
        if (object.isNew()) {
            result = true;
        } else {
            try {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                service.remove(object);
                result = true;
            } catch (OpenVPMSException exception) {
                String title = Messages.get("imobject.delete.failed.title");
                ErrorHelper.show(title, exception);
            }
        }
        deleted |= result;
        return result;
    }

    /**
     * Determines if the object has been deleted.
     *
     * @return <code>true</code> if the object has been deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Determines if the object has been changed.
     *
     * @return <code>true</code> if the object has been changed
     */
    public boolean isModified() {
        return editors.isModified() || properties.isModified()
                || getObject().isNew();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        editors.clearModified();
        properties.clearModified();
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        editors.addModifiableListener(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        editors.removeModifiableListener(listener);
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        return editors.isValid();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <code>true</code> if the object and its descendents are valid
     *         otherwise <code>false</code>
     */
    public boolean validate(Validator validator) {
        boolean valid = validator.validate(editors);
        if (valid) {
            // validate each property not associated with an editor
            for (Property property : properties.getProperties()) {
                String name = property.getDescriptor().getName();
                if (editors.getEditor(name) == null) {
                    if (!validator.validate(property)) {
                        valid = false;
                        break;
                    }
                }
            }
        }
        return valid;
    }

    /**
     * Cancel any edits. Once complete, query methods may be invoked, but the
     * behaviour of other methods is undefined..
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * Determines if editing was cancelled.
     *
     * @return <code>true</code> if editing was cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    public Component getComponent() {
        return getView().getComponent();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or <code>null</code> if the editor hasn't been
     *         rendered
     */
    public FocusGroup getFocusGroup() {
        return getView().getFocusGroup();
    }

    /**
     * Add a property change listener.
     *
     * @param name     the property name to listen on
     * @param listener the listener
     */
    public void addPropertyChangeListener(String name,
                                          PropertyChangeListener listener) {
        if (propertyChangeNotifier == null) {
            propertyChangeNotifier = new PropertyChangeSupport(this);
        }
        propertyChangeNotifier.addPropertyChangeListener(name, listener);
    }

    /**
     * Remove a property change listener.
     *
     * @param name     the property name to remove the listener for
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(String name,
                                             PropertyChangeListener listener) {
        if (propertyChangeNotifier != null) {
            propertyChangeNotifier.removePropertyChangeListener(
                    name, listener);
        }
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    protected boolean doSave() {
        boolean saved = false;
        if (saveObject()) {
            saved = saveChildren();
        }
        return saved;
    }

    /**
     * Save any modified child Saveable instances.
     *
     * @return <code>true</code> if the save was successful
     */
    protected boolean saveChildren() {
        for (Saveable saveable : editors.getModifiedSaveable()) {
            if (!saveable.save()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Save the object.
     *
     * @return <code>true</code> if the save was successful
     */
    protected boolean saveObject() {
        IMObject object = getObject();
        return SaveHelper.save(object);
    }

    /**
     * Returns the child editors.
     *
     * @return the child editors
     */
    protected Editors getEditors() {
        return editors;
    }

    /**
     * Report a bound property update to any registered listeners. No event is
     * fired if old and new are equal and non-null.
     *
     * @param name     the name of the property that was changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    protected void firePropertyChange(String name, Object oldValue,
                                      Object newValue) {
        if (propertyChangeNotifier != null) {
            propertyChangeNotifier.firePropertyChange(name, oldValue,
                                                      newValue);
        }
    }

    /**
     * Returns the view, creating it if it doesn't exist.
     *
     * @return the view
     */
    protected IMObjectView getView() {
        if (viewer == null) {
            viewer = createView(object);
        }
        return viewer;
    }

    /**
     * Creates an {@link IMObjectView} to render the object.
     *
     * @param object the object to view
     * @return a new object view
     */
    protected IMObjectView createView(IMObject object) {
        IMObjectLayoutStrategy layout = createLayoutStrategy();
        IMObjectView view;
        view = new AbstractIMObjectView(object, properties, parent, layout) {
            @Override
            protected Component createComponent() {
                lookups.clear();
                Component component = super.createComponent();
                onLayoutCompleted();
                return component;
            }

            protected LayoutContext getLayoutContext() {
                return context;
            }
        };
        if (layout instanceof ExpandableLayoutStrategy) {
            view.getComponent(); // make sure the component is rendered.
            ExpandableLayoutStrategy exp = (ExpandableLayoutStrategy) layout;
            Button button = exp.getButton();
            if (button != null) {
                button.addActionListener(getLayoutChangeListener());
            }
        }
        return view;
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return context;

    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategyFactory layoutStrategy
                = context.getLayoutStrategyFactory();
        return layoutStrategy.create(getObject(), getParent());
    }

    /**
     * Change the layout.
     */
    protected void onLayout() {
        Component oldValue = getComponent();
        if (getView().getLayout() instanceof ExpandableLayoutStrategy) {
            ExpandableLayoutStrategy expandable = (ExpandableLayoutStrategy) getView().getLayout();
            expandable.setShowOptional(!expandable.isShowOptional());
            getView().setLayout(expandable);
            Button button = expandable.getButton();
            if (button != null) {
                button.addActionListener(getLayoutChangeListener());
            }
        } else {
            IMObjectLayoutStrategy layout = createLayoutStrategy();
            getView().setLayout(layout);
            if (layout instanceof ExpandableLayoutStrategy) {
                ExpandableLayoutStrategy exp = (ExpandableLayoutStrategy) layout;
                Button button = exp.getButton();
                if (button != null) {
                    button.addActionListener(getLayoutChangeListener());
                }
            }
        }
        Component newValue = getComponent();
        firePropertyChange(COMPONENT_CHANGED_PROPERTY, oldValue, newValue);
    }

    /**
     * Invoked when layout has completed. This can be used to perform
     * processing that requires all editors to be created.
     */
    protected void onLayoutCompleted() {
    }

    /**
     * Invoked to update derived fields.
     *
     * @param modified the modified object.
     */
    protected void updateDerivedFields(Modifiable modified) {
        if (modified instanceof Property) {
            editors.removeModifiableListener(derivedFieldRefresher);
            try {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                service.deriveValues(getObject());

                for (Property property : properties.getProperties()) {
                    if (modified != property
                            && property.getDescriptor().isDerived()) {
                        property.refresh();
                    }
                }
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
            editors.addModifiableListener(derivedFieldRefresher);
        }
    }

    protected void refreshLookups(SelectField source) {
        for (SelectField lookup : lookups) {
            if (source != lookup) {
                LookupListModel model = (LookupListModel) lookup.getModel();
                model.refresh();
            }
        }
    }

    /**
     * Returns the layout change action listener.
     *
     * @return the layout change listener
     */
    protected ActionListener getLayoutChangeListener() {
        if (layoutChangeListener == null) {
            layoutChangeListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onLayout();
                }
            };
        }
        return layoutChangeListener;
    }

    /**
     * Helper to return a node descriptor from the archetype, given its name.
     *
     * @param name the node descriptor's name
     * @return the corresponding node descriptor, or <code>null</code> if it
     *         doesn't exist
     */
    protected NodeDescriptor getDescriptor(String name) {
        return getArchetypeDescriptor().getNodeDescriptor(name);
    }

    /**
     * Helper to return a property, given its descriptor's name.
     *
     * @param name the descriptor's name
     * @return the property corresponding to <code>name</code> or
     *         <code>null</code> if none exists
     */
    protected Property getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Helper to return an editor associated with a property, given the property
     * name.
     *
     * @param name the property name
     * @return the editor corresponding to <code>name</code> or
     *         </code>null</code> if none exists
     */
    protected Editor getEditor(String name) {
        if (editors.isEmpty()) {
            // make sure the component has been laid out to ensure
            // the editors are created
            getComponent();
        }
        return editors.getEditor(name);
    }


    private class ComponentFactory extends NodeEditorFactory {

        /**
         * Construct a new <code>ComponentFactory</code>.
         *
         * @param context the layout context
         */
        public ComponentFactory(LayoutContext context) {
            super(editors, context);
        }

        /**
         * Returns a component to edit a lookup property.
         *
         * @param property the lookup property
         * @param context  the parent object
         * @return a component to edit the property
         */
        @Override
        protected Editor getSelectEditor(Property property,
                                         IMObject context) {
            Editor editor = super.getSelectEditor(property, context);
            SelectField lookup = (SelectField) editor.getComponent();
            lookup.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    refreshLookups((SelectField) event.getSource());
                }
            });
            lookups.add(lookup);
            return editor;
        }
    }
}
