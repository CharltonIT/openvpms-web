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

package org.openvpms.web.component.im.util;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * Handles deletion of {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public abstract class IMObjectDeleter {

    /**
     * The context.
     */
    private final Context context;

    /**
     * Relationship archetype short names to exclude.
     */
    private String[] excludeRelationships;

    /**
     * Constructs an {@link IMObjectDeleter}.
     *
     * @param context the context
     */
    public IMObjectDeleter(Context context) {
        this.context = context;
    }

    /**
     * Sets the archetype short names of relationships to exclude when considering if an entity should be deleted.
     *
     * @param relationships the relationships to exclude
     */
    public void setExcludeRelationships(String... relationships) {
        this.excludeRelationships = relationships;
    }

    /**
     * Attempts to delete an object.
     *
     * @param object   the object to delete
     * @param help     the help context
     * @param listener the listener to notify
     */
    public <T extends IMObject> void delete(T object, HelpContext help, IMObjectDeletionListener<T> listener) {
        try {
            if (object instanceof Entity) {
                Entity entity = (Entity) object;
                String participations;
                if (TypeHelper.isA(entity, DocumentArchetypes.DOCUMENT_TEMPLATE)) {
                    participations = "participation.documentTemplate";
                } else {
                    participations = "participation.*";
                }
                if (hasParticipations(entity, participations) || hasRelationships(entity)) {
                    if (object.isActive()) {
                        deactivate(object, listener, help);
                    } else {
                        deactivated(object, help);
                    }
                } else {
                    remove(object, listener, help);
                }
            } else {
                remove(object, listener, help);
            }
        } catch (Throwable exception) {
            listener.failed(object, exception);
        }
    }

    /**
     * Invoked to remove an object.
     *
     * @param object   the object to remove
     * @param listener the listener to notify
     * @param help     the help context
     */
    protected abstract <T extends IMObject> void remove(T object, IMObjectDeletionListener<T> listener,
                                                        HelpContext help);

    /**
     * Invoked to deactivate an object.
     *
     * @param object   the object to deactivate
     * @param listener the listener
     * @param help     the help context
     */
    protected abstract <T extends IMObject> void deactivate(T object, IMObjectDeletionListener<T> listener,
                                                            HelpContext help);

    /**
     * Invoked when an object cannot be de deleted, and has already been deactivated.
     *
     * @param object the object
     * @param help   the help context
     */
    protected abstract <T extends IMObject> void deactivated(T object, HelpContext help);


    /**
     * Performs the deletion in a transaction context.
     *
     * @param object   the object to delete
     * @param listener the listener to notify
     * @param help     the help context
     * @return {@code true} if the object was deleted successfully
     */
    protected <T extends IMObject> boolean doRemove(final T object, final IMObjectDeletionListener<T> listener,
                                                    HelpContext help) {
        boolean removed = false;
        try {
            DefaultLayoutContext layout = new DefaultLayoutContext(true, context, help);
            layout.setDeletionListener(new DeletionListenerAdapter<T>(object, listener));
            final IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(object, layout);
            TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
            Boolean result = template.execute(new TransactionCallback<Boolean>() {
                public Boolean doInTransaction(TransactionStatus status) {
                    return editor.delete();
                }
            });
            removed = (result != null) && result;
            if (removed) {
                listener.deleted(object);
            }
        } catch (Throwable exception) {
            listener.failed(object, exception);
        }
        return removed;
    }

    /**
     * Performs deactivation.
     *
     * @param object   the object to deactivate
     * @param listener the listener to notify
     * @return {@code true} if the object was deactivated successfully
     */
    protected <T extends IMObject> boolean doDeactivate(T object, IMObjectDeletionListener<T> listener) {
        boolean deactivated = false;
        try {
            object.setActive(false);
            deactivated = SaveHelper.save(object);
            if (deactivated) {
                listener.deactivated(object);
            }
        } catch (Throwable exception) {
            listener.failed(object, exception);
        }
        return deactivated;
    }


    /**
     * Determines if an entity has participations of a particular archetype.
     *
     * @param entity    the entity
     * @param shortName the participation archetype short name.
     * @return {@code true} if the entity has participations, otherwise {@code false}
     * @throws ArchetypeServiceException for any error
     */
    protected boolean hasParticipations(Entity entity, String shortName) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, false);
        query.add(new ObjectRefNodeConstraint("entity", entity.getObjectReference()));
        query.setMaxResults(1);
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        return !service.get(query).getResults().isEmpty();
    }

    /**
     * Determines if an entity is the target of any entity links.
     *
     * @param entity the entity
     * @return {@code true} if the entity is a target of at least one entity link
     */
    protected boolean hasEntityLinks(Entity entity) {
        ArchetypeQuery query = new ArchetypeQuery("entityLink.*", false, false);
        query.add(new ObjectRefNodeConstraint("target", entity.getObjectReference()));
        query.setMaxResults(1);
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        return !service.get(query).getResults().isEmpty();
    }

    /**
     * Determines if an entity has any relationships where it is the source, and the relationship isn't excluded.
     *
     * @param entity the entity
     * @return {@code true} if the entity has relationships where it is the source, otherwise {@code false}
     */
    protected boolean hasRelationships(Entity entity) {
        boolean result = false;
        if (excludeRelationships == null || excludeRelationships.length == 0) {
            result = !entity.getSourceEntityRelationships().isEmpty();
        } else {
            for (EntityRelationship relationship : entity.getSourceEntityRelationships()) {
                if (!TypeHelper.isA(relationship, excludeRelationships)) {
                    result = true;
                    break;
                }
            }
        }
        if (!result) {
            result = hasEntityLinks(entity);
        }
        return result;
    }

    /**
     * Deletion listener that ensures type parameters aren't violated.
     */
    private static class DeletionListenerAdapter<T extends IMObject>
            implements IMObjectDeletionListener<IMObject> {

        /**
         * The object to pass to the listener.
         */
        private T object;

        /**
         * The listener to delegate to.
         */
        private IMObjectDeletionListener<T> listener;

        /**
         * Constructs a {@code DeletionListenerAdapter}.
         *
         * @param object   the object to pass to the listener
         * @param listener the listener to delegate to
         */
        public DeletionListenerAdapter(T object, IMObjectDeletionListener<T> listener) {
            this.object = object;
            this.listener = listener;
        }

        /**
         * Notifies that an object has been deleted.
         *
         * @param object the deleted object
         */
        public void deleted(IMObject object) {
            listener.deleted(this.object);
        }

        /**
         * Notifies that an object has been deactivated.
         *
         * @param object the deactivated object
         */
        public void deactivated(IMObject object) {
            listener.deactivated(this.object);
        }

        /**
         * Notifies that an object has failed to be deleted.
         *
         * @param object the object that failed to be deleted
         * @param cause  the reason for the failure
         */
        public void failed(IMObject object, Throwable cause) {
            listener.failed(this.object, cause);
        }

        /**
         * Notifies that an object has failed to be deleted.
         *
         * @param object the object that failed to be deleted
         * @param cause  the reason for the failure
         * @param editor the editor that performed the deletion
         */
        public void failed(IMObject object, Throwable cause, IMObjectEditor editor) {
            listener.failed(this.object, cause, editor);
        }
    }
}
