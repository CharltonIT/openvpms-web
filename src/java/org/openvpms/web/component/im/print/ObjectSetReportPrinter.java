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

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.IMReport;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ObjectSetReporter;


/**
 * Prints reports for {@link ObjectSet}s generated by {@link IMReport}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ObjectSetReportPrinter extends TemplatedIMPrinter<ObjectSet> {

    /**
     * Constructs an <tt>ObjectSetReportPrinter</tt>.
     *
     * @param set      the set to print
     * @param template the document template to use
     * @throws ArchetypeServiceException for any archetype service error
     */
    public ObjectSetReportPrinter(Iterable<ObjectSet> set, DocumentTemplate template) {
        super(new ObjectSetReporter(set, template));
    }

    /**
     * Creates a new <tt>ObjectSetReportPrinter</tt>.
     *
     * @param set     the set to print
     * @param locator the document template locator
     * @throws ArchetypeServiceException for any archetype service error
     */
    public ObjectSetReportPrinter(Iterable<ObjectSet> set, DocumentTemplateLocator locator) {
        super(new ObjectSetReporter(set, locator));
    }

    /**
     * Creates a new <tt>ObjectSetReportPrinter</tt>.
     * TODO - should be removed as it is dependendent on the global context
     *
     * @param set       the set to print
     * @param shortName the archetype short name to determine the template to use
     * @throws ArchetypeServiceException for any archetype service error
     */
    public ObjectSetReportPrinter(Iterable<ObjectSet> set, String shortName) {
        this(set, new ContextDocumentTemplateLocator(shortName, GlobalContext.getInstance()));
    }

}
