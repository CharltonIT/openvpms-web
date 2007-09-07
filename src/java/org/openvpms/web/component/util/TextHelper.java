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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import java.util.regex.Pattern;


/**
 * Helper to filter characters from text.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TextHelper {

    /**
     * Pattern that matches strings containing control characters.
     */
    private static final Pattern CONTAINS = Pattern.compile(".*[\\p{Cntrl}].*");

    /**
     * Pattern used to replace control characters.
     */
    private static final Pattern REPLACE = Pattern.compile("[\\p{Cntrl}]");


    /**
     * Determines if a string contains control characters.
     *
     * @param str the string. May be <tt>null</tt>
     * @return <tt>true</tt> if thje string contains
     */
    public static boolean hasControlChars(String str) {
        return (str != null) && CONTAINS.matcher(str).matches();
    }

    /**
     * Replaces any control characters (i.e, those that match the regexp
     * pattern <tt>\p{Cntrl}</tt>).
     *
     * @param str  the string. May be <tt>null</tt>
     * @param with the string to replace the characters with
     * @return the string with any
     */
    public static String replaceControlChars(String str, String with) {
        if (str != null) {
            str = REPLACE.matcher(str).replaceAll(with);
        }
        return str;
    }
}
