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

import java.util.Date;
import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import static org.openvpms.component.system.common.query.Constraints.gt;
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.ne;
import static org.openvpms.component.system.common.query.Constraints.or;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DefaultActQuery;

/**
 *
 * @author benjamincharlton
 */
public class EstimateQuery extends DefaultActQuery<Act> {
    
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
    
    public EstimateQuery() {
        super(SHORT_NAMES, STATUSES);
        setDefaultSortConstraint(DEFAULT_SORT);
        setConstraints(or(ne("status",EstimateActStatus.CANCELLED),or(ne("status",EstimateActStatus.INVOICED),or(isNull("endTime"), gt("endTime", new Date())))));
    }
}
