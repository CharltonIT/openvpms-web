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

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.util.CollectionHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;

import java.util.Date;


/**
 * Query component for {@link Act} instances.
 *
 * @author Tim Anderson
 */
public abstract class ActQuery<T> extends AbstractArchetypeQuery<T> {

    /**
     * The default sort constraint. Sorts on descending start time and ascending id.
     */
    public static final SortConstraint[] DESCENDING_START_TIME = {
            new NodeSortConstraint("startTime", false),
            new NodeSortConstraint("id")
    };

    /**
     * The participant node name.
     */
    private String participant;

    /**
     * The entity participation short name. May be {@code null}
     */
    private String participation;

    /**
     * The id of the entity to search for.
     */
    private IMObjectReference entityId;

    /**
     * Short names which are always queried on.
     */
    private String[] requiredShortNames;

    /**
     * The statuses to query on.
     */
    private String[] statuses;

    /**
     * The act status lookups. May be {@code null}
     */
    private final ActStatuses statusLookups;

    /**
     * The status dropdown.
     */
    private LookupField statusSelector;


    /**
     * Constructs an {@link ActQuery}.
     *
     * @param entity        the entity to search for. May be {@code null}
     * @param participant   the participant node name. May be {@code null}
     * @param participation the entity participation short name. May be {@code null}
     * @param shortNames    the act short names
     * @param statuses      the act status lookups. May be {@code null}
     * @param type          the type that this query returns
     */
    public ActQuery(Entity entity, String participant, String participation, String[] shortNames, ActStatuses statuses,
                    Class type) {
        super(shortNames, type);
        setParticipantConstraint(entity, participant, participation);
        this.statusLookups = statuses;
        this.statuses = new String[0];
        setDefaultSortConstraint(DESCENDING_START_TIME);
    }

    /**
     * Constructs an {@link ActQuery}.
     *
     * @param entity        the entity to search for. May be {@code null}
     * @param participant   the participant node name. May be {@code null}
     * @param participation the entity participation short name. May be {@code null}
     * @param shortNames    the act short names
     * @param type          the type that this query returns
     */
    public ActQuery(Entity entity, String participant, String participation, String[] shortNames, Class type) {
        this(entity, participant, participation, shortNames, true, new String[0], type);
    }

    /**
     * Constructs an {@link ActQuery}.
     *
     * @param entity        the entity to search for. May be {@code null}
     * @param participant   the participant node name. May be {@code null}
     * @param participation the entity participation short name. May be {@code null}
     * @param shortNames    the act short names
     * @param primaryOnly   if {@code true} only primary archetypes will be queried
     * @param statuses      the act statuses to search on. May be {@code empty}
     * @param type          the type that this query returns
     */
    public ActQuery(Entity entity, String participant, String participation, String[] shortNames, boolean primaryOnly,
                    String[] statuses, Class type) {
        super(shortNames, primaryOnly, type);
        setParticipantConstraint(entity, participant, participation);
        this.statuses = statuses;
        statusLookups = null;
        setDefaultSortConstraint(DESCENDING_START_TIME);
    }

    /**
     * Sets the entity to search for.
     *
     * @param entity the entity to search for. May be {@code null}
     */
    public void setEntity(Entity entity) {
        entityId = (entity != null) ? entity.getObjectReference() : null;
    }

    /**
     * Sets the short names which are required to be queried.
     * These are short names that are always queried independent of the
     * short name selector.
     *
     * @param shortNames the short names. May be {@code null}
     */
    public void setRequiredShortNames(String[] shortNames) {
        requiredShortNames = shortNames;
    }

    /**
     * Sets the status to query on.
     *
     * @param status the status to query on. May be {@code null}
     */
    public void setStatus(String status) {
        if (status == null) {
            statuses = new String[0];
        } else {
            statuses = new String[]{status};
        }
        updateStatusSelector(status);
    }

    /**
     * Sets the statuses to query on.
     *
     * @param statuses the statuses to query on. May be {@code null}
     */
    public void setStatuses(String[] statuses) {
        if (statuses == null) {
            this.statuses = new String[0];
        } else {
            this.statuses = statuses;
        }
    }

    /**
     * Performs the query.
     * If constraining acts to a particular entity, the entity must be non-null
     * or a {@code null} will be returned.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set. May be {@code null}
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<T> query(SortConstraint[] sort) {
        ResultSet<T> result = null;

        if (participant == null || entityId != null) {
            result = createResultSet(sort);
        }
        return result;
    }

    /**
     * Returns the archetypes to select from.
     *
     * @return the archetypes to select from
     */
    @Override
    public ShortNameConstraint getArchetypes() {
        ShortNameConstraint result;
        ShortNameConstraint constraint = super.getArchetypes();
        if (requiredShortNames == null) {
            result = constraint;
        } else {
            // need to add the required short names
            String[] shortNames = constraint.getShortNames();
            shortNames = CollectionHelper.concat(shortNames, requiredShortNames);
            result = new ShortNameConstraint(shortNames, constraint.isPrimaryOnly(), true);
        }
        return result;
    }

    /**
     * Returns the archetypes to query, based on whether a short name has been
     * selected or not. The constraint alias is set to 'act'.
     *
     * @return the archetypes to query
     */
    @Override
    protected ShortNameConstraint getArchetypeConstraint() {
        ShortNameConstraint archetypes;
        String type = getShortName();

        if (type == null) {
            archetypes = getArchetypes();
        } else {
            archetypes = getArchetypeConstraint(type);
        }
        archetypes.setAlias("act");
        return archetypes;
    }

    /**
     * Sets the participation constraint. This may be used to only return
     * acts for a particular entity, or if {@code null}, all entities.
     *
     * @param entity    the entity. May be {@code null}
     * @param nodeName  the participation node. If {@code null}, indicates to not constrain acts to an entity
     * @param shortName the participation archetype short name. May be {@code null}
     */
    protected void setParticipantConstraint(Entity entity, String nodeName, String shortName) {
        setEntity(entity);
        participant = nodeName;
        participation = shortName;
    }

    /**
     * Returns the participant constraint.
     *
     * @return the participant constraint, or {@code null} if not restricting
     *         to a particular entity
     */
    protected ParticipantConstraint getParticipantConstraint() {
        ParticipantConstraint result = null;
        IMObjectReference entityId = getEntityId();
        if (entityId != null) {
            result = new ParticipantConstraint(participant, participation,
                                               entityId);
        }
        return result;
    }

    /**
     * Returns the archetype constraint, containing a subset of the available
     * short names. This includes the required short names plus a user specified
     * short name.
     *
     * @param shortName the short name
     * @return the archetype constraint
     */
    protected ShortNameConstraint getArchetypeConstraint(String shortName) {
        ShortNameConstraint result;
        if (requiredShortNames == null) {
            result = new ShortNameConstraint(shortName, isPrimaryOnly(), true);
        } else {
            String[] shortNames = CollectionHelper.concat(requiredShortNames, shortName);
            result = new ShortNameConstraint(shortNames, isPrimaryOnly(), true);
        }
        return result;
    }

    /**
     * Returns the entity reference.
     *
     * @return the entity reference. May be {@code null}
     */
    protected IMObjectReference getEntityId() {
        return entityId;
    }

    /**
     * Returns the 'from' date.
     *
     * @return the 'from' date, or {@code null} to query all dates
     */
    protected Date getFrom() {
        return null;
    }

    /**
     * Returns the 'to' date.
     *
     * @return the 'to' date, or {@code null} to query all dates
     */
    protected Date getTo() {
        return null;
    }

    /**
     * Returns the act statuses lookups.
     *
     * @return the act status lookups. May be {@code null}
     */
    protected ActStatuses getStatusLookups() {
        return statusLookups;
    }

    /**
     * Returns the act statuses to query.
     *
     * @return the act statuses to query
     */
    protected String[] getStatuses() {
        String[] statuses = this.statuses;
        if (statuses.length == 0 && statusLookups != null) {
            if (statusLookups.getExcluded() != null) {
                statuses = new String[]{statusLookups.getExcluded()};
            }
        }
        return statuses;
    }

    /**
     * Determines if act statuses are being excluded.
     *
     * @return {@code true} to exclude acts with status in
     *         {@link #getStatuses()}; otherwise include them.
     */
    protected boolean excludeStatuses() {
        String[] statuses = getStatuses();
        if (statuses.length == 1 && statusLookups != null) {
            String excluded = statusLookups.getExcluded();
            return statuses[0].equals(excluded);
        }
        return false;
    }

    /**
     * Invoked when a status is selected.
     */
    protected void onStatusChanged() {
        setStatus(statusSelector.getSelectedCode());
    }

    /**
     * Adds a status selector to the container, if status lookups have been specified.
     *
     * @param container the container
     */
    protected void addStatusSelector(Component container) {
        if (statusLookups != null) {
            statusSelector = LookupFieldFactory.create(statusLookups, true);
            statusSelector.addActionListener(new ActionListener() {
                public void onAction(ActionEvent e) {
                    onStatusChanged();
                }
            });
            if (statuses != null) {
                if (statuses.length == 0) {
                    updateStatusSelector(null);
                } else if (statuses.length == 1) {
                    updateStatusSelector(statuses[0]);
                }
            } else {
                String defaultStatus = statusSelector.getSelectedCode();
                if (defaultStatus != null) {
                    setStatus(defaultStatus);
                }
            }
        }

        if (statusSelector != null) {
            container.add(LabelFactory.create("actquery.status"));
            container.add(statusSelector);
            getFocusGroup().add(statusSelector);
        }
    }

    /**
     * Returns the status selector, if it exists.
     *
     * @return the status selector, or {@code null} if one doesn't exist
     */
    protected LookupField getStatusSelector() {
        return statusSelector;
    }

    /**
     * Sets the selected status in the status selector, if it exists.
     *
     * @param status the status to select
     */
    private void updateStatusSelector(String status) {
        if (statusSelector != null) {
            statusSelector.setSelected(status);
        }
    }

}
