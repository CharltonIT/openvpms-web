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

package org.openvpms.web.app.patient.reminder;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.component.processor.BatchProcessor;


/**
 * A {@link BatchProcessor} that renders to a component.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface BatchProcessorComponent extends BatchProcessor {

    /**
     * The processor title.
     *
     * @return the processor title
     */
    String getTitle();

    /**
     * The component.
     *
     * @return the component
     */
    Component getComponent();

    /**
     * Restarts processing.
     */
    void restart();

}
