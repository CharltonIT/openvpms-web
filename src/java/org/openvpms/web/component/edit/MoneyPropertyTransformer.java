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

import org.openvpms.web.resource.util.Messages;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * Handler for money nodes.
 * todo - workaround for OBF-54
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 04:38:04Z $
 */
public class MoneyPropertyTransformer extends PropertyTransformer {

    /**
     * Construct a new <code>NumericPropertyTransformer</code>.
     *
     * @param descriptor the node descriptor.
     */
    public MoneyPropertyTransformer(NodeDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Transform an object to the required type, performing validation.
     *
     * @param object the object to convert
     * @return the transformed object, or <code>object</code> if no
     *         transformation is required
     * @throws ValidationException if the object is invalid
     */
    public Object apply(Object object) throws ValidationException {
        Object result;
        try {
            if (object instanceof String) {
                result = new Money((String) object);
            } else if (object instanceof BigDecimal) {
                result = new Money(object.toString());
            } else if (object instanceof BigInteger) {
                result = new Money((BigInteger) object);
            } else if (object instanceof Short
                    || object instanceof Integer
                    || object instanceof Long) {
                result = new Money(((Number) object).longValue());
            } else if (object instanceof Float || object instanceof Double) {
                result = new Money(((Number) object).doubleValue());
            } else {
                throw getException(null);
            }
        } catch (Throwable exception) {
            throw getException(exception);
        }

        return result;
    }

    private ValidationException getException(Throwable exception) {
        NodeDescriptor node = getDescriptor();
        String message = Messages.get("node.error.invalidnumeric",
                                      node.getDisplayName());
        ValidationError error = new ValidationError(node.getName(),
                                                    message);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        errors.add(error);
        ValidationException.ErrorCode code
                = ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype;
        if (exception != null) {
            return new ValidationException(errors, code, exception);
        }
        return new ValidationException(errors, code);
    }

}
