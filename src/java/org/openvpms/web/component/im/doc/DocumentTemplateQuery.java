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

package org.openvpms.web.component.im.doc;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractQuery;
import org.openvpms.web.component.im.query.PreloadedResultSet;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.ResultSet;

import java.util.ArrayList;
import java.util.List;


/**
 * Query for <em>entity.documentTemplate</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentTemplateQuery extends AbstractQuery<Entity> {

    /**
     * The archetype short name to filter on. May be <code>null</code>.
     */
    private String archetypeShortName;


    /**
     * Construct a new <code>DocumentTemplateQuery</code>.
     *
     * @throws ArchetypeQueryException if the short name don't match any
     *                                 archetypes
     */
    public DocumentTemplateQuery() {
        super(new String[]{"entity.documentTemplate"});
        QueryFactory.initialise(this);
    }

    /**
     * Sets the archetype to filter on.
     *
     * @param shortName the archetype short name. May be <code>null</code>
     */
    public void setArchetype(String shortName) {
        archetypeShortName = shortName;
    }

    /**
     * Returns the archetype to filter on.
     *
     * @return the archetype to filter on. May be <code>null</code>
     */
    public String getArchetype() {
        return archetypeShortName;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set
     */
    @Override
    public ResultSet<Entity> query(SortConstraint[] sort) {
        ResultSet<Entity> result;
        if (archetypeShortName == null) {
            result = super.query(sort);
        } else {
            ArchetypeQuery query = new ArchetypeQuery("entity.documentTemplate",
                                                      false, true);
            query.setFirstRow(0);
            query.setNumOfRows(ArchetypeQuery.ALL_ROWS);
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            List<IMObject> rows = service.get(query).getRows();
            List<Entity> objects = new ArrayList<Entity>();
            for (IMObject object : rows) {
                EntityBean bean = new EntityBean((Entity) object);
                String archetype = bean.getString("archetype");
                if (StringUtils.isEmpty(archetype)
                        || TypeHelper.matches(archetypeShortName, archetype)) {
                    objects.add((Entity) object);
                }
            }
            result = new PreloadedResultSet<Entity>(objects, getMaxRows());
        }
        return result;
    }
}
