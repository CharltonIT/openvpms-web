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

package org.openvpms.hl7;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * Represents a HL7 inbound or outbound connector.
 *
 * @author Tim Anderson
 */
public abstract class Connector {

    /**
     * The sending application.
     */
    private final String sendingApplication;

    /**
     * The sending facility.
     */
    private final String sendingFacility;

    /**
     * The receiving application.
     */
    private final String receivingApplication;

    /**
     * The receiving facility.
     */
    private final String receivingFacility;


    /**
     * Constructs a {@link Connector}.
     *
     * @param sendingApplication   the sending application
     * @param sendingFacility      the sending facility
     * @param receivingApplication the receiving application
     * @param receivingFacility    the receiving facility
     */
    public Connector(String sendingApplication, String sendingFacility, String receivingApplication,
                     String receivingFacility) {
        this.sendingApplication = sendingApplication;
        this.sendingFacility = sendingFacility;
        this.receivingApplication = receivingApplication;
        this.receivingFacility = receivingFacility;
    }

    /**
     * Returns the sending application.
     *
     * @return the sending application
     */
    public String getSendingApplication() {
        return sendingApplication;
    }

    /**
     * Returns the sending facility.
     *
     * @return the sending facility
     */
    public String getSendingFacility() {
        return sendingFacility;
    }

    /**
     * Returns the receiving application.
     *
     * @return the receiving application
     */
    public String getReceivingApplication() {
        return receivingApplication;
    }

    /**
     * Returns the receiving facility.
     *
     * @return the receiving facility
     */
    public String getReceivingFacility() {
        return receivingFacility;
    }

    /**
     * Returns the connector reference.
     *
     * @return the connector reference
     */
    public abstract IMObjectReference getReference();

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = super.equals(obj);
        if (!result && obj instanceof Connector) {
            Connector other = (Connector) obj;
            result = ObjectUtils.equals(sendingApplication, other.sendingApplication)
                     && ObjectUtils.equals(sendingFacility, other.sendingFacility)
                     && ObjectUtils.equals(receivingApplication, other.receivingApplication)
                     && ObjectUtils.equals(receivingFacility, other.receivingFacility);
        }
        return result;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        return hashCode(builder).toHashCode();
    }

    /**
     * Builds the hash code.
     *
     * @param builder the hash code builder
     * @return the builder
     */
    protected HashCodeBuilder hashCode(HashCodeBuilder builder) {
        return builder.append(sendingApplication).append(sendingFacility)
                .append(receivingApplication).append(receivingFacility);
    }
}