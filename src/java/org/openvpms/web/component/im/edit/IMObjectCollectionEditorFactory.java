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

import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ArchetypeHandlers;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;


/**
 * Factory for {@link IMObjectCollectionEditor} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectCollectionEditorFactory {

    /**
     * Editor implementations.
     */
    private static ArchetypeHandlers _editors;

    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(IMObjectCollectionEditorFactory.class);


    /**
     * Prevent construction.
     */
    private IMObjectCollectionEditorFactory() {
    }

    /**
     * Creates a new editor.
     *
     * @param collection the collection to edit
     * @param object     the parent of the collection
     * @param context    the layout context. May be <code>null</code>
     * @return an editor for <code>collection</code>
     */
    public static IMObjectCollectionEditor create(CollectionProperty collection,
                                                  IMObject object,
                                                  LayoutContext context) {
        IMObjectCollectionEditor result = null;

        NodeDescriptor descriptor = collection.getDescriptor();
        String[] shortNames = DescriptorHelper.getShortNames(descriptor);
        Set<Class> matches = new HashSet<Class>();
        for (String shortName : shortNames) {
            Class clazz = getEditors().getHandler(shortName);
            if (clazz != null) {
                matches.add(clazz);
            }
        }
        if (matches.size() == 1) {
            Class clazz = matches.toArray(new Class[0])[0];
            Constructor ctor = getConstructor(clazz, collection, object,
                                              context);
            if (ctor != null) {
                try {
                    result = (IMObjectCollectionEditor) ctor.newInstance(
                            collection, object, context);
                } catch (Throwable throwable) {
                    _log.error(throwable, throwable);
                }
            } else {
                _log.error("No valid constructor found for class: "
                        + clazz.getName());
            }
        }
        if (result == null) {
            result = new DefaultIMObjectCollectionEditor(collection, object,
                                                         context);
        }
        return result;
    }

    /**
     * Returns the editors.
     *
     * @return the editors
     */
    private static synchronized ArchetypeHandlers getEditors() {
        if (_editors == null) {
            _editors = new ArchetypeHandlers(
                    "IMObjectCollectionEditorFactory.properties",
                    IMObjectCollectionEditor.class);
        }
        return _editors;
    }

    /**
     * Returns a constructor to construct a new editor.
     *
     * @param type       the editor type
     * @param collection the collection property
     * @param object     the parent of the collection
     * @param context    the layout context. May be <code>null</code>
     * @return a constructor to construct the editor, or <code>null</code> if
     *         none can be found
     */
    private static Constructor getConstructor(Class type,
                                              CollectionProperty collection,
                                              IMObject object,
                                              LayoutContext context) {
        Constructor[] ctors = type.getConstructors();

        for (Constructor ctor : ctors) {
            // check parameters
            Class[] ctorTypes = ctor.getParameterTypes();
            if (ctorTypes.length == 3) {
                Class ctorCollection = ctorTypes[0];
                Class ctorObj = ctorTypes[1];
                Class ctorLayout = ctorTypes[2];

                if (ctorCollection.isAssignableFrom(collection.getClass())
                        && ctorObj.isAssignableFrom(object.getClass())
                        && ((context != null && ctorLayout.isAssignableFrom(
                        context.getClass()))
                        || (context == null
                        && LayoutContext.class.isAssignableFrom(ctorLayout)))) {
                    return ctor;
                }
            }
        }
        return null;
    }

}
