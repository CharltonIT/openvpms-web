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

package org.openvpms.web.component.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.test.TestHelper;


/**
 * {@link StringPropertyTransformer} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-09-04 05:00:00Z $
 */
public class StringPropertyTransformerTestCase
        extends AbstractAppTest {

    /**
     * Tests {@link StringPropertyTransformer#apply}.
     */
    public void testApply() {
        Party person = TestHelper.createCustomer();
        NodeDescriptor descriptor = getDescriptor(person, "name");
        StringPropertyTransformer handler
                = new StringPropertyTransformer(person, descriptor);

        assertNull(handler.apply(null));
        assertEquals("", handler.apply(""));
        assertEquals("abc", handler.apply("abc"));

        assertEquals("1", handler.apply(1));
    }

    /**
     * Tests macro expansion by {@link StringPropertyTransformer#apply}.
     */
    public void testMacroExpansion() {
        Party person = TestHelper.createCustomer();
        NodeDescriptor descriptor = getDescriptor(person, "name");
        StringPropertyTransformer handler
                = new StringPropertyTransformer(person, descriptor);

        Object text1 = handler.apply("@macro1");
        assertEquals("macro 1 text", text1);

        Object text2 = handler.apply("@macro2");
        assertEquals("onetwothree", text2);

        Object text3 = handler.apply("@macro1 @macro2");
        assertEquals("macro 1 text onetwothree", text3);

        Object text4 = handler.apply("@displayName");
        assertEquals("Customer(Person)", text4);

        // verifies that invalid macros don't expand
        Object text5 = handler.apply("@invalidNode");
        assertEquals("@invalidNode", text5);

        // verifies that non-existent macros don't expand
        Object text6 = handler.apply("@non existent");
        assertEquals("@non existent", text6);
    }

    /**
     * Helper to return a node descriptor.
     *
     * @param object the object
     * @param node   the node name
     * @return the node descriptor
     */
    protected NodeDescriptor getDescriptor(IMObject object, String node) {
        ArchetypeDescriptor type
                = DescriptorHelper.getArchetypeDescriptor(object);
        assertNotNull(type);
        NodeDescriptor result = type.getNodeDescriptor(node);
        assertNotNull(result);
        return result;
    }

}
