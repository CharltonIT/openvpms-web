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

package org.openvpms.web.component.workflow;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.print.IMObjectPrinterFactory;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterListener;
import org.openvpms.web.component.im.print.InteractiveIMObjectPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;


/**
 * Prints an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PrintIMObjectTask extends AbstractTask {

    /**
     * The short name of the object to print.
     */
    private final String shortName;

    /**
     * Creates a new <code>PrintIMObjectTask</code>.
     *
     * @param shortName the short name of the object to print
     */
    public PrintIMObjectTask(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     */
    public void start(final TaskContext context) {
        IMObject object = context.getObject(shortName);
        if (object != null) {
            IMPrinter<IMObject> printer = IMObjectPrinterFactory.create(
                    object);
            InteractiveIMPrinter<IMObject> iPrinter
                    = new InteractiveIMObjectPrinter<IMObject>(printer);

            iPrinter.setListener(new IMPrinterListener() {
                public void printed() {
                    notifyCompleted();
                }

                public void cancelled() {
                    notifyCancelled();
                }

                public void failed(Throwable cause) {
                    notifyCancelled();
                }
            });
            iPrinter.print();
        } else {
            notifyCancelled();
        }
    }

}
