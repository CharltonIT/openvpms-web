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

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.IMReport;
import org.openvpms.report.IMReportException;
import org.openvpms.report.IMReportFactory;
import org.openvpms.web.system.ServiceHelper;


/**
 * Prints reports for {@link ObjectSet}s generated by {@link IMReport}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ObjectSetReportPrinter
        extends AbstractIMReportPrinter<ObjectSet> {

    /**
     * The document template.
     */
    private final Document template;


    /**
     * Constructs a new <code>ObjectSetReportPrinter</code>.
     *
     * @param set      the set to print
     * @param template the document template
     */
    public ObjectSetReportPrinter(ObjectSet set, Document template) {
        super(set);
        this.template = template;
    }

    /**
     * Returns the default printer for an object.
     *
     * @return the default printer, or <code>null</code> if there is
     *         none defined
     */
    public String getDefaultPrinter() {
        return null;
    }

    /**
     * Creates a new report.
     *
     * @return a new report
     * @throws IMReportException         for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMReport<ObjectSet> createReport() {
        return IMReportFactory.createObjectSetReport(
                template, ArchetypeServiceHelper.getArchetypeService(),
                ServiceHelper.getDocumentHandlers());
    }

}
