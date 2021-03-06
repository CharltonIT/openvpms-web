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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Date;


/**
 * Result set for {@link Act}s.
 *
 * @author Tim Anderson
 */
public class ActResultSet<T extends Act> extends AbstractActResultSet<T> {

    /**
     * Constructs an {@link ActResultSet}.
     *
     * @param archetypes  the act archetype constraint
     * @param participant the participant constraint
     * @param from        the act start-from date. May be {@code null}
     * @param to          the act start-to date. May be {@code null}
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be {@code null}
     */
    public ActResultSet(ShortNameConstraint archetypes,
                        ParticipantConstraint participant,
                        Date from, Date to,
                        String[] statuses, int pageSize,
                        SortConstraint[] sort) {
        this(archetypes, null, participant, from, to, statuses, pageSize, sort);
    }

    /**
     * Constructs an {@link ActResultSet}.
     *
     * @param archetypes  the act archetype constraint
     * @param value       the value to query on. May be {@code null}
     * @param participant the participant constraint
     * @param from        the act start-from date. May be {@code null}
     * @param to          the act start-to date. May be {@code null}
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be {@code null}
     */
    public ActResultSet(ShortNameConstraint archetypes,
                        String value,
                        ParticipantConstraint participant,
                        Date from, Date to,
                        String[] statuses, int pageSize,
                        SortConstraint[] sort) {
        super(archetypes, value, participant, from, to, statuses, false, null, pageSize, sort,
              new DefaultQueryExecutor<T>());
    }

    /**
     * Constructs an {@link ActResultSet}.
     *
     * @param archetypes  the act archetype constraint
     * @param participant the participant constraint. May be {@code null}
     * @param from        the act start-from date. May be {@code null}
     * @param to          the act start-to date. May be {@code null}
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param exclude     if {@code true} exclude acts with status in
     *                    {@code statuses}; otherwise include them.
     * @param constraints additional query constraints. May be {@code null}
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be {@code null}
     */
    public ActResultSet(ShortNameConstraint archetypes,
                        ParticipantConstraint participant,
                        Date from, Date to,
                        String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        super(archetypes, participant, from, to, statuses, exclude, constraints,
              pageSize, sort, new DefaultQueryExecutor<T>());
    }

    /**
     * Constructs an {@link ActResultSet}.
     *
     * @param archetypes  the act archetype constraint
     * @param value       the value to query on. May be {@code null}
     * @param participant the participant constraint. May be {@code null}
     * @param from        the act start-from date. May be {@code null}
     * @param to          the act start-to date. May be {@code null}
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param exclude     if {@code true} exclude acts with status in
     *                    {@code statuses}; otherwise include them.
     * @param constraints additional query constraints. May be {@code null}
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be {@code null}
     */
    public ActResultSet(ShortNameConstraint archetypes,
                        String value,
                        ParticipantConstraint participant,
                        Date from, Date to,
                        String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        super(archetypes, value, participant, from, to, statuses, exclude, constraints,
              pageSize, sort, new DefaultQueryExecutor<T>());
    }

    /**
     * Constructs a {@link ActResultSet}.
     *
     * @param archetypes   the act archetype constraint
     * @param participants the participant constraints. May be {@code null}
     * @param from         the act start-from date. May be {@code null}
     * @param to           the act start-to date. May be {@code null}
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param exclude      if {@code true} exclude acts with status in
     *                     {@code statuses}; otherwise include them.
     * @param constraints  additional query constraints. May be {@code null}
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be {@code null}
     */
    public ActResultSet(ShortNameConstraint archetypes,
                        ParticipantConstraint[] participants,
                        Date from, Date to,
                        String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        this(archetypes, null, participants, from, to, statuses, exclude, constraints, pageSize, sort);
    }

    /**
     * Constructs a {@link ActResultSet}.
     *
     * @param archetypes   the act archetype constraint
     * @param participants the participant constraints. May be {@code null}
     * @param from         the act start-from date. May be {@code null}
     * @param to           the act start-to date. May be {@code null}
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param exclude      if {@code true} exclude acts with status in
     *                     {@code statuses}; otherwise include them.
     * @param constraints  additional query constraints. May be {@code null}
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be {@code null}
     */
    public ActResultSet(ShortNameConstraint archetypes,
                        String value,
                        ParticipantConstraint[] participants,
                        Date from, Date to,
                        String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        super(archetypes, value, participants, from, to, statuses, exclude,
              constraints, pageSize, sort, new DefaultQueryExecutor<T>());
    }

    /**
     * Constructs a {@link ActResultSet}.
     *
     * @param archetypes   the act archetype constraint
     * @param participants the participant constraints. May be {@code null}
     * @param times        the time constraints. May be {@code null}
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param exclude      if {@code true} exclude acts with status in
     *                     {@code statuses}; otherwise include them.
     * @param constraints  additional query constraints. May be {@code null}
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be {@code null}
     */
    public ActResultSet(ShortNameConstraint archetypes,
                        ParticipantConstraint[] participants,
                        IConstraint times, String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        this(archetypes, null, participants, times, statuses, exclude, constraints, pageSize, sort);
    }

    /**
     * Constructs a {@link ActResultSet}.
     *
     * @param archetypes   the act archetype constraint
     * @param value        the value to query on. May be {@code null}
     * @param participants the participant constraints. May be {@code null}
     * @param times        the time constraints. May be {@code null}
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param exclude      if {@code true} exclude acts with status in
     *                     {@code statuses}; otherwise include them.
     * @param constraints  additional query constraints. May be {@code null}
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be {@code null}
     */
    public ActResultSet(ShortNameConstraint archetypes,
                        String value,
                        ParticipantConstraint[] participants,
                        IConstraint times, String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        super(archetypes, value, participants, times, statuses, exclude, constraints,
              pageSize, sort, new DefaultQueryExecutor<T>());
    }

}
