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

package org.openvpms.web.component.im.print;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.util.ArchetypeHandler;
import org.openvpms.web.component.im.util.ArchetypeHandlers;


/**
 * A factory for {@link IMObjectPrinter} instances. The factory is configured to return
 * specific {@link IMObjectPrinter} implementations based on the supplied criteria, with
 * {@link IMObjectReportPrinter} returned if no implementation matches.
 * <p/>
 * The factory is configured using a <em>IMObjectPrinterFactory.properties</em> file,
 * located in the class path. The file contains pairs of archetype short names
 * and their corresponding printer implementations. Short names may be wildcarded
 * e.g:
 * <p/>
 * <table>
 * <tr><td>party.*</td><td>org.openvpms.web.component.im.print.APrinter</td></tr>
 * <tr><td>lookup.*</td><td>org.openvpms.web.component.im.print.BPrinter</td></tr>
 * <tr><td>act.customerAccountChargesInvoice.*</td><td>org.openvpms.web.component.im.print.CPrinter</td></tr>
 * </table>
 * <p/>
 * Multiple <em>IMObjectPrinterFactory.properties</em> may be used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-17 06:51:11Z $
 */
public final class IMObjectPrinterFactory {

    /**
     * IMObjectPrinter implementations.
     */
    private static ArchetypeHandlers<IMObjectPrinter> printers;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(
            IMObjectPrinterFactory.class);


    /**
     * Prevent construction.
     */
    private IMObjectPrinterFactory() {
    }

    /**
     * Construct a new {@link IMObjectPrinter}.
     * <p/>
     * IMObjectPrinter implementations must provide a public constructor
     * accepting the object to print
     *
     * @param object the object to print
     * @return a new printer
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> IMObjectPrinter<T> create(T object) {
        String[] shortNames = {object.getArchetypeId().getShortName()};
        shortNames = DescriptorHelper.getShortNames(shortNames);
        ArchetypeHandler<IMObjectPrinter> handler
                = getPrinters().getHandler(shortNames);
        IMObjectPrinter<T> result = null;
        if (handler != null) {
            try {
                result = handler.create();
            } catch (Throwable throwable) {
                log.error(throwable, throwable);
            }
        }
        if (result == null) {
            result = new IMObjectReportPrinter<T>(object);
        }
        return result;
    }

    /**
     * Returns the printer implementations.
     *
     * @return the printers
     */
    private static ArchetypeHandlers<IMObjectPrinter> getPrinters() {
        if (printers == null) {
            printers = new ArchetypeHandlers<IMObjectPrinter>(
                    "IMObjectPrinterFactory.properties",
                    IMObjectPrinter.class);
        }
        return printers;
    }

}
