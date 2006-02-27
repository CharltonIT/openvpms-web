package org.openvpms.web.component.im.edit;

import java.util.List;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.DefaultQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.select.Selector;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;


/**
 * An editor for {@link EntityRelationship}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class RelationshipEditor extends AbstractIMObjectEditor {

    /**
     * The relationship.
     */
    private final EntityRelationship _relationship;

    /**
     * The entity representing the source of the relationship.
     */
    private Entity _source;

    /**
     * The entity representing the target of the relationship.
     */
    private Entity _target;


    /**
     * Construct a new <code>RelationshipEditor</code>.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param descriptor   the parent descriptor
     * @param showAll      if <code>true</code> show optional and required
     *                     fields; otherwise show required fields.
     */
    protected RelationshipEditor(EntityRelationship relationship, IMObject parent,
                                 NodeDescriptor descriptor, boolean showAll) {
        super(relationship, parent, descriptor, showAll);
        _relationship = relationship;
        ArchetypeDescriptor archetype = getArchetypeDescriptor();
        NodeDescriptor sourceDesc = archetype.getNodeDescriptor("source");
        NodeDescriptor targetDesc = archetype.getNodeDescriptor("target");

        IMObject source = getObject(_relationship.getSource(), sourceDesc);
        IMObject target = getObject(_relationship.getTarget(), targetDesc);

        IMObject edited = Context.getInstance().getCurrent();
        boolean srcReadOnly = true;
        if (source == null || !source.equals(edited)) {
            srcReadOnly = false;
        }
        if (source != null && _relationship.getSource() == null) {
            _relationship.setSource(new IMObjectReference(source));
        }
        _source = create(source, sourceDesc, srcReadOnly);

        boolean targetReadOnly = true;
        if (target == null || !target.equals(edited) || target.equals(source)) {
            targetReadOnly = false;
        }
        if (target != null && _relationship.getTarget() == null) {
            _relationship.setTarget(new IMObjectReference(target));
        }
        _target = create(target, targetDesc, targetReadOnly);
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object     the object to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        NodeDescriptor descriptor, boolean showAll) {
        IMObjectEditor result = null;
        if (object instanceof EntityRelationship) {
            result = new RelationshipEditor((EntityRelationship) object, parent,
                    descriptor, showAll);
        }
        return result;
    }

    /**
     * Creates the layout strategy.
     *
     * @param showAll if <code>true</code> show required and optional fields;
     *                otherwise show required fields.
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy(boolean showAll) {
        return new LayoutStrategy(showAll);
    }

    /**
     * Pops up a dialog to select an entity.
     *
     * @param entity the entity wrapper
     */
    protected void onSelect(final Entity entity) {
        NodeDescriptor descriptor = entity.getDescriptor();
        Query query = new DefaultQuery(descriptor.getArchetypeRange());
        final Browser browser = new Browser(query);
        String title = Messages.get("imobject.select.title",
                descriptor.getDisplayName());
        final BrowserDialog popup = new BrowserDialog(title, browser);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                IMObject object = popup.getSelected();
                if (object != null) {
                    onSelected(entity, object);
                }
            }
        });

        popup.show();
    }

    /**
     * Invoked when an entity object is selected.
     *
     * @param entity the entity wrapper
     * @param object the entity object
     */
    protected void onSelected(Entity entity, IMObject object) {
        entity.setObject(object);
        IMObjectReference reference = new IMObjectReference(object);
        if (entity == _source) {
            _relationship.setSource(reference);
        } else {
            _relationship.setTarget(reference);
        }
    }

    /**
     * Returns an object given its reference and descriptor. If the reference is
     * null, determines if the descriptor matches that of the current object
     * being edited and returns that instead.
     *
     * @param reference  the object reference. May be <code>null</code>
     * @param descriptor the node descriptor
     * @return the object matching <code>reference</code>, or
     *         <code>descriptor</code>, or <code>null</code> if there is no
     *         matches
     */
    private IMObject getObject(IMObjectReference reference,
                               NodeDescriptor descriptor) {
        IMObject result = null;
        if (reference == null) {
            result = match(descriptor);
        } else {
            IMObject edit = Context.getInstance().getCurrent();
            if (edit != null) {
                if (edit.getArchetypeId().equals(reference.getArchetypeId())
                        && edit.getUid() == reference.getUid()) {
                    result = edit;
                }
            }
            if (result == null) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                result = service.get(reference);
            }
        }
        return result;
    }

    /**
     * Creates a new entity representing one side of the relationship.
     *
     * @param object     the object. May be <code>null</code>
     * @param descriptor the entity's descriptor
     * @param readOnly   if <code>true</code>, the enity cannot be changed
     * @return a new <code>Entity</code>
     */
    private Entity create(IMObject object, NodeDescriptor descriptor,
                          boolean readOnly) {
        final Entity result;
        result = new Entity(object, descriptor, readOnly);
        if (!readOnly) {
            result.getSelect().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSelect(result);
                }
            });
        }
        return result;
    }

    /**
     * Determines if the current object being edited matches archetype range of
     * the specified descriptor.
     *
     * @param descriptor the node descriptor
     * @return the current object being edited, or <code>null</code> if its type
     *         doesn't matches the specified descriptor's archetype range
     */
    private IMObject match(NodeDescriptor descriptor) {
        IMObject result = null;
        String[] range = descriptor.getArchetypeRange();
        IMObject object = Context.getInstance().getCurrent();
        if (object != null) {
            String shortName = object.getArchetypeId().getShortName();
            for (int i = 0; i < range.length; ++i) {
                if (range[i].equals(shortName)) {
                    result = object;
                    break;
                }
            }
        }
        return result;
    }


    /**
     * EntityRelationship layout strategy. Displays the source and target nodes
     * before any others.
     */
    private class LayoutStrategy extends ExpandableLayoutStrategy {

        /**
         * Construct a new <code>LayoutStrategy</code>.
         *
         * @param showOptional if <code>true</code> show optional fields as well
         *                     as mandatory ones.
         */
        public LayoutStrategy(boolean showOptional) {
            super(showOptional);
            ChainedNodeFilter filter = new ChainedNodeFilter();
            filter.add(new BasicNodeFilter(showOptional));
            filter.add(new NamedNodeFilter("source", "target"));
            setNodeFilter(filter);
        }

        /**
         * Lays out child components in a 2x2 grid.
         *
         * @param object      the parent object
         * @param descriptors the child descriptors
         * @param container   the container to use
         * @param factory     the component factory
         */
        @Override
        protected void doSimpleLayout(IMObject object,
                                      List<NodeDescriptor> descriptors,
                                      Component container,
                                      IMObjectComponentFactory factory) {
            Grid grid = GridFactory.create(2);
            add(grid, _source.getDescriptor().getDisplayName(),
                    _source.getComponent());
            add(grid, _target.getDescriptor().getDisplayName(),
                    _target.getComponent());
            for (NodeDescriptor descriptor : descriptors) {
                Component component = factory.create(object, descriptor);
                add(grid, descriptor.getDisplayName(), component);
            }

            Row group = RowFactory.create(grid, getButtonRow());
            container.add(group);
        }

    }

    /**
     * Wrapper for a source/target entity in a relationship.
     */
    private class Entity extends Selector {

        /**
         * The entity's descriptor.
         */
        private NodeDescriptor _descriptor;


        /**
         * Construct a new <code>Entity</code>.
         *
         * @param entity     the entity. May be <code>null</code>
         * @param descriptor the entity descriptor
         * @param readOnly   if <code>true<code> don't render the select button
         */
        public Entity(IMObject entity, NodeDescriptor descriptor,
                      boolean readOnly) {
            super(readOnly ? ButtonStyle.HIDE : ButtonStyle.RIGHT);
            _descriptor = descriptor;
            getComponent();
            setObject(entity);
        }

        /**
         * Returns the entity's descriptor.
         *
         * @return the entity's descriptor
         */
        public NodeDescriptor getDescriptor() {
            return _descriptor;
        }

    }

}
