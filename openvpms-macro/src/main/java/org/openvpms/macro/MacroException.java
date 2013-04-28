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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.macro;

import org.openvpms.component.system.common.exception.OpenVPMSException;

/**
 * Macro exception.
 *
 * @author Tim Anderson
 */
public class MacroException extends OpenVPMSException {

    /**
     * Constructs a {@code MacroException}.
     *
     * @param message the error message
     */
    public MacroException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code MacroException}.
     *
     * @param message the error message
     * @param cause   the root cause
     */
    public MacroException(String message, Throwable cause) {
        super(message, cause);
    }
}
