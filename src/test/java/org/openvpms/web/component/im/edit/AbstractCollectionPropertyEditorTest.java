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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.IMObjectProperty;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.test.AbstractAppTest;


/**
 * {@link CollectionPropertyEditor} test.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractCollectionPropertyEditorTest
        extends AbstractAppTest {

    /**
     * Tests the behaviour of performing query operations on an empty
     * collection editor
     */
    public void testEmpty() {
        IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        CollectionPropertyEditor editor = createEditor(property, parent);

        assertTrue("Collection should be empty", editor.getObjects().isEmpty());
        assertFalse("Collection shouldn't be modified", editor.isModified());
        assertSame(property, editor.getProperty());
        assertFalse("Collection not saved", editor.isSaved());
        if (property.getMinCardinality() > 0) {
            assertFalse("Collection should be invalid", editor.isValid());
        } else {
            assertFalse("Collection should be valid", editor.isValid());
        }
    }

    /**
     * Tests {@link CollectionPropertyEditor#save}.
     */
    public void testSave() {
        IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        assertTrue("Require collection with min cardinality >= 0",
                   property.getMinCardinality() >= 0);
        CollectionPropertyEditor editor = createEditor(property, parent);

        IMObject element = createObject();
        editor.add(element);
        assertEquals(1, editor.getObjects().size());
        assertSame(element, editor.getObjects().get(0));

        assertTrue("Collection should be valid", editor.isValid());
        assertTrue("Collection should be modified", editor.isModified());

        assertTrue("Failed to save parent", SaveHelper.save(parent));
        assertTrue("Failed to save collection", editor.save());

        assertTrue(editor.isSaved());
        assertFalse(editor.isModified());

        // make sure the element has saved
        assertEquals("Retrieved element doesnt match that saved",
                     element, get(element));

        // now retrieve parent and verify collection matches the original
        IMObject savedParent = get(parent);
        assertNotNull(savedParent);
        CollectionPropertyEditor saved = createEditor(
                getCollectionProperty(savedParent), savedParent);
        assertEquals(1, saved.getObjects().size());
        assertTrue(saved.getObjects().contains(element));

        assertFalse("Collection shouldn't be modified", saved.isModified());
        assertFalse("Collection not saved", saved.isSaved());
        assertTrue("Collection should be valid", saved.isValid());
    }

    /**
     * Tests {@link CollectionPropertyEditor#remove}.
     */
    public void testRemove() {
        IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        assertTrue("Require collection with min cardinality >= 0",
                   property.getMinCardinality() >= 0);
        CollectionPropertyEditor editor = createEditor(property, parent);

        IMObject elt1 = createObject();
        IMObject elt2 = createObject();

        editor.add(elt1);
        editor.add(elt2);

        assertEquals(2, editor.getObjects().size());
        assertTrue(editor.getObjects().contains(elt1));
        assertTrue(editor.getObjects().contains(elt2));
        assertTrue(editor.isValid());
        assertTrue(editor.isModified());

        assertTrue("Failed to save parent", SaveHelper.save(parent));
        assertTrue("Failed to save collection", editor.save());

        // make sure the elements have saved
        assertEquals("Retrieved element1 doesnt match that saved",
                     elt1, get(elt1));
        assertEquals("Retrieved element2 doesnt match that saved",
                     elt2, get(elt2));

        // now remove elt1, save and verify that it is no longer available
        editor.remove(elt1);
        assertEquals(1, editor.getObjects().size());
        assertFalse(editor.getObjects().contains(elt1));
        assertTrue(editor.getObjects().contains(elt2));

        assertTrue("Failed to save parent", SaveHelper.save(parent));
        assertTrue("Failed to save collection", editor.save());
        assertNull("element1 wasnt deleted", get(elt1));

        // now retrieve parent and verify collection matches the original
        IMObject savedParent = get(parent);
        assertNotNull(savedParent);
        CollectionPropertyEditor saved = createEditor(
                getCollectionProperty(savedParent), savedParent);
        assertEquals(1, saved.getObjects().size());
        assertTrue(saved.getObjects().contains(elt2));

        assertFalse("Collection shouldn't be modified", saved.isModified());
        assertFalse("Collection not saved", saved.isSaved());
        assertTrue("Collection should be valid", saved.isValid());
    }

    /**
     * Tests {@link CollectionPropertyEditor#remove} on a collection that has
     * been saved and reloaded.
     */
    public void testRemoveAfterReload() {
        IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        assertTrue("Require collection with min cardinality >= 0",
                   property.getMinCardinality() >= 0);
        CollectionPropertyEditor editor = createEditor(property, parent);

        IMObject elt1 = createObject();
        IMObject elt2 = createObject();

        editor.add(elt1);
        editor.add(elt2);

        assertEquals(2, editor.getObjects().size());
        assertTrue(editor.getObjects().contains(elt1));
        assertTrue(editor.getObjects().contains(elt2));
        assertTrue(editor.isValid());
        assertTrue(editor.isModified());

        assertTrue("Failed to save parent", SaveHelper.save(parent));
        assertTrue("Failed to save collection", editor.save());

        // make sure the elements have saved
        assertEquals("Retrieved element1 doesnt match that saved",
                     elt1, get(elt1));
        assertEquals("Retrieved element2 doesnt match that saved",
                     elt2, get(elt2));

        // reload parent and collection
        parent = get(parent);
        editor = createEditor(getCollectionProperty(parent), parent);
        assertEquals(2, editor.getObjects().size());
        elt1 = get(elt1);
        elt2 = get(elt2);

        // now remove elt1, save and verify that it is no longer available
        editor.remove(elt1);
        assertEquals(1, editor.getObjects().size());
        assertFalse(editor.getObjects().contains(elt1));
        assertTrue(editor.getObjects().contains(elt2));

        assertTrue("Failed to save parent", SaveHelper.save(parent));
        assertTrue("Failed to save collection", editor.save());
        assertNull("element1 wasnt deleted", get(elt1));

        // now retrieve parent and verify collection matches the original
        IMObject savedParent = get(parent);
        assertNotNull(savedParent);
        CollectionPropertyEditor saved = createEditor(
                getCollectionProperty(savedParent), savedParent);
        assertEquals(1, saved.getObjects().size());
        assertTrue(saved.getObjects().contains(elt2));

        assertFalse("Collection shouldn't be modified", saved.isModified());
        assertFalse("Collection not saved", saved.isSaved());
        assertTrue("Collection should be valid", saved.isValid());
    }

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    protected abstract IMObject createParent();

    /**
     * Returns the name of the collection node.
     *
     * @return the node name
     */
    protected abstract String getCollectionNode();

    /**
     * Returns the collection property.
     *
     * @param parent the parent of the collection
     * @return the collection property
     */
    protected CollectionProperty getCollectionProperty(
            IMObject parent) {
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(
                parent);
        assertNotNull(archetype);
        NodeDescriptor node = archetype.getNodeDescriptor(getCollectionNode());
        assertNotNull(node);
        return new IMObjectProperty(parent, node);
    }

    /**
     * Returns an editor for a collection property.
     *
     * @param property the collection property
     * @param parent   the parent of the collection
     * @return a new editor for the property
     */
    protected abstract CollectionPropertyEditor createEditor(
            CollectionProperty property, IMObject parent);

    /**
     * Returns an object to add to the collection.
     *
     * @return a new object to add to the collection
     */
    protected abstract IMObject createObject();

    /**
     * Helper to retrieve an object from the archetype service using its
     * reference.
     *
     * @param object the object to retrieve
     * @return the corresponding object or <code>null</code> if none is found
     */
    protected IMObject get(IMObject object) {
        return IMObjectHelper.reload(object);
    }

}
