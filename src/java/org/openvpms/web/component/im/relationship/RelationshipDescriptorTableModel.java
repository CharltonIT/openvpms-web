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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Table model for object relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipDescriptorTableModel<T extends IMObjectRelationship>
        extends DescriptorTableModel<T> {

    /**
     * Determines if the target or source of the relationship should be
     * displayed.
     */
    private final boolean displayTarget;


    /**
     * Creates a new <tt>RelationshipDescriptorTableModel</tt>.
     * <p/>
     * Enables selection if the context is in edit mode, or <tt>null</tt>
     *
     * @param shortNames    the archetype short names
     * @param context       the layout context. May be <tt>null</tt>
     * @param displayTarget if <tt>true</tt> display the target node,
     *                      otherwise display the source node
     */
    public RelationshipDescriptorTableModel(String[] shortNames,
                                            LayoutContext context,
                                            boolean displayTarget) {
        this(shortNames, context, displayTarget,
             (context == null) || context.isEdit());
    }

    /**
     * Creates a new <tt>RelationshipDescriptorTableModel</tt>.
     *
     * @param shortNames      the archetype short names
     * @param context         the layout context. May be <tt>null</tt>
     * @param displayTarget   if <tt>true</tt> display the target node,
     *                        otherwise display the source node
     * @param enableSelection if <tt>true</tt>, enable selection, otherwise
     *                        disable it
     */
    public RelationshipDescriptorTableModel(String[] shortNames,
                                            LayoutContext context,
                                            boolean displayTarget,
                                            boolean enableSelection) {
        super(context);
        this.displayTarget = displayTarget;
        setTableColumnModel(createColumnModel(shortNames, getLayoutContext()));
        setEnableSelection(enableSelection);
    }

    /**
     * Returns the node names for a set of archetypes.
     * <p/>
     * This is prepended by the <em>source</em> or <em>target</em> node,
     * depending on the value of the {@link #displayTarget} parameter passed at
     * construction.
     *
     * @param archetypes the archetype descriptors
     * @param context    the layout context
     * @return the node names for the archetypes
     */
    @Override
    protected List<String> getNodeNames(List<ArchetypeDescriptor> archetypes,
                                        LayoutContext context) {
        List<String> result = new ArrayList<String>(
                super.getNodeNames(archetypes, context));
        String entity = (displayTarget) ? "target" : "source";
        result.add(0, entity);
        return result;
    }

}