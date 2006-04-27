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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.util.AbstractIMObjectCopyHandler;
import org.openvpms.web.component.im.util.IMObjectCopier;
import org.openvpms.web.component.im.util.IMObjectCopyHandler;


/**
 * {@link IMObjectCopyHandler} that creates reversals for acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractActReversalHandler
        extends AbstractIMObjectCopyHandler {

    /**
     * Determines if the act is a debit or a credit.
     */
    private final boolean _debit;

    /**
     * Map of debit types to their corresponding credit types.
     */
    private final String[][] _shortNames;


    /**
     * Construct a new <code>SupplierActReversalHandler</code>.
     *
     * @param debit      if <code>true</code> indicates that the act is a debit;
     *                   <code>false</code> indicates its a credit
     * @param shortNames a list of archetype short names. The first column
     *                   indicates the 'debit' short name, the second the
     *                   corresponding 'credit short name
     */
    public AbstractActReversalHandler(boolean debit, String[][] shortNames) {
        _debit = debit;
        _shortNames = shortNames;
    }

    /**
     * Determines how {@link IMObjectCopier} should treat an object.
     *
     * @param object  the source object
     * @param service the archetype service
     * @return <code>object</code> if the object shouldn't be copied,
     *         <code>null</code> if it should be replaced with
     *         <code>null</code>, or a new instance if the object should be
     *         copied
     */
    public IMObject getObject(IMObject object, IArchetypeService service) {
        IMObject result;
        if (object instanceof Act || object instanceof ActRelationship
            || object instanceof Participation) {
            String shortName = object.getArchetypeId().getShortName();
            for (String[] map : _shortNames) {
                String debitType = map[0];
                String creditType = map[1];
                if (_debit) {
                    if (debitType.equals(shortName)) {
                        shortName = creditType;
                        break;
                    }
                } else {
                    if (creditType.equals(shortName)) {
                        shortName = debitType;
                        break;
                    }
                }
            }
            result = service.create(shortName);
            if (result == null) {
                throw new ArchetypeServiceException(
                        ArchetypeServiceException.ErrorCode.FailedToCreateArchetype,
                        new String[]{shortName});
            }
        } else {
            result = object;
        }
        return result;
    }
}
