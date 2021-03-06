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

package org.openvpms.web.component.im.sms;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.List;

/**
 * SMS helper methods.
 *
 * @author Tim Anderson
 */
public class SMSHelper {

    /**
     * Determines if SMS is configured for the practice.
     * <p/>
     * TODO - this should be moved into a practice service
     *
     * @return {@code true} if SMS is configured, otherwise {@code false}
     */
    public static boolean isSMSEnabled(Party practice) {
        boolean enabled = false;
        if (practice != null) {
            EntityBean bean = new EntityBean(practice);
            List<IMObjectReference> refs = bean.getNodeTargetEntityRefs("SMS");
            for (IMObjectReference ref : refs) {
                if (IMObjectHelper.isActive(ref)) {
                    enabled = true;
                    break;
                }
            }
        }
        return enabled;
    }

    /**
     * Returns the phone number from a contact, extracting any formatting.
     *
     * @param contact the phone contact
     * @return the phone number. May be {@code null}
     */
    public static String getPhone(Contact contact) {
        IMObjectBean bean = new IMObjectBean(contact);
        String areaCode = bean.getString("areaCode");
        String phone = bean.getString("telephoneNumber");
        String result = null;
        if (!StringUtils.isEmpty(areaCode)) {
            result = areaCode;
            if (!StringUtils.isEmpty(phone)) {
                result += phone;
            }
        } else if (!StringUtils.isEmpty(phone)) {
            result = phone;
        }
        result = getPhone(result);
        return result;
    }

    /**
     * Returns the phone number from a string, extracting any formatting.
     *
     * @param phone the formatted phone number
     * @return the phone number. May be {@code null}
     */
    public static String getPhone(String phone) {
        String result = phone;
        if (!StringUtils.isEmpty(result)) {
            // strip any spaces, hyphens, and brackets, and any characters after the last digit.
            result = result.replaceAll("[\\s\\-()]", "").replaceAll("[^\\d\\+].*", "");
        }
        return result;
    }
}
