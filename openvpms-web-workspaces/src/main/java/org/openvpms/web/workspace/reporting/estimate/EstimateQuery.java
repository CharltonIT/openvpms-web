/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.estimate;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.query.AndConstraint;
import static org.openvpms.component.system.common.query.Constraints.and;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gt;
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.or;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.focus.FocusHelper;

/**
 *
 * @author benjamincharlton
 */
public class EstimateQuery extends DefaultActQuery<Act> {
    
    private final IConstraint constraints;
    /**
     * The act short names.
     */
    public static final String[] SHORT_NAMES = new String[]{
        EstimateArchetypes.ESTIMATE
        };
    private final SortConstraint[] DEFAULT_SORT = {
        new NodeSortConstraint("startTime")
    };
    private static final ActStatuses STATUSES
        = new ActStatuses(EstimateArchetypes.ESTIMATE);
    
    private String searchText;
    
    private Long  searchId;
    
    public EstimateQuery() {
        super(SHORT_NAMES, STATUSES);
        String[] statusConstraints = {EstimateActStatus.IN_PROGRESS, 
            EstimateActStatus.COMPLETED,
            EstimateActStatus.POSTED};
        setStatuses(statusConstraints);
        setDefaultSortConstraint(DEFAULT_SORT);
        constraints = or(isNull("endTime"), gt("endTime", new Date()));
        this.setConstraints(constraints);
        searchText = null;
        searchId = null;
    }
    
    @Override
    protected void doLayout(Component container) {
        addStatusSelector(container);
        addSearchField(container);
        getSearchField().setToolTipText("Searches on estimate ID or Title");
        getSearchField().addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        searchText = getSearchField().getText();
                    }
                });
        FocusHelper.setFocus(getSearchField());
    }
    private void setSearchConstraints() {
        searchText = getSearchField().getText();
        if (StringUtils.isNotBlank(searchText)) {
            if(StringUtils.isNumeric(searchText)) {
                searchId = Long.valueOf(getSearchField().getText());
                searchText = null;
                setConstraints(eq("id", searchId));
                String[] statusConstraints = {EstimateActStatus.IN_PROGRESS, 
                                                EstimateActStatus.COMPLETED, EstimateActStatus.POSTED,                                           EstimateActStatus.INVOICED, EstimateActStatus.CANCELLED};
                setStatuses(statusConstraints);
            }
            else {
                searchId = null;
                searchText = getWildcardedText(getSearchField());
                setConstraints(eq("title",searchText));
            }
        }else {
            setConstraints(constraints);
        }
        
    }
        protected ResultSet<Act> createResultSet(SortConstraint[] sort) {
        setSearchConstraints();
        return new ActResultSet<Act>(getArchetypeConstraint(),
                                   getParticipantConstraint(),
                                   getFrom(), getTo(), getStatuses(),
                                   excludeStatuses(), getConstraints(),
                                   getMaxResults(), sort);
    } 
}
