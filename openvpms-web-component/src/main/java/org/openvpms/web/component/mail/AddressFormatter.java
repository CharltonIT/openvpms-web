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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;


import org.openvpms.component.business.domain.im.party.Contact;

/**
 * Formats email addresses for display.
 *
 * @author Tim Anderson
 */
public interface AddressFormatter {

    /**
     * Returns the email address of a contact.
     *
     * @param contact the email contact. May be {@code null}
     * @return the the contact's email address, or {@code null} if none is found
     */
    String getAddress(Contact contact);

    /**
     * Returns the contact name.
     *
     * @param contact the email contact
     * @return the contact name. May be {@code null}
     */
    String getName(Contact contact);

    /**
     * Returns the qualified name.
     * <p/>
     * This includes the party name and contact name, if a contact name is specified.
     *
     * @param contact the email contact
     * @return the contact name. May be {@code null}
     */
    String getQualifiedName(Contact contact);

    /**
     * Returns the contact name and address.
     *
     * @param contact the email contact
     * @return the contact name and address. May {@code null}
     */
    String getNameAddress(Contact contact);

    /**
     * Returns the qualified name and address.
     * <p/>
     * This includes the party name and contact name, if a contact name is specified.
     *
     * @param contact the email contact
     * @return the qualified name and address
     */
    String getQualifiedNameAddress(Contact contact);

    /**
     * Formats an email address contact.
     *
     * @param contact the email contact
     * @return the formatted contact
     */
    String format(Contact contact);


    /**
     * Returns the type of a contact.
     *
     * @param contact the email contact
     * @return the type of the contact. May be {@code null}
     */
    String getType(Contact contact);
}