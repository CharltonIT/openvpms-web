package org.openvpms.web.component.edit;

import java.util.List;

import org.openvpms.component.business.service.archetype.ValidationError;


/**
 * General validation interface..
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision$ $Date$
 */
public interface Validator {

    /**
     * Perform validation, returning a list of errors if the object is invalid.
     *
     * @param value the value to validate
     * @return a list of error messages if the object is invalid; or an empty
     *         list if valid
     */
    List<ValidationError> validate(Object value);

    /**
     * Determines if the object is valid.
     *
     * @param value the value to validate
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    boolean isValid(Object value);

}
