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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.report;

import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.system.ServiceHelper;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.NotFound;
import static org.openvpms.report.ReportException.ErrorCode.NoTemplateForArchetype;


/**
 * Generates {@link Document}s from one or more {@link ObjectSet}s.
 *
 * @author Tim Anderson
 */
public class ObjectSetReporter extends TemplatedReporter<ObjectSet> {

    /**
     * Constructs an {@link ObjectSetReporter} for a collection of objects.
     *
     * @param objects  the objects to print
     * @param template the document template to use
     */
    public ObjectSetReporter(Iterable<ObjectSet> objects, DocumentTemplate template) {
        super(objects, template);
    }

    /**
     * Constructs an {@link ObjectSetReporter} for a collection of objects.
     *
     * @param objects the objects to print
     * @param locator the document template locator
     */
    public ObjectSetReporter(Iterable<ObjectSet> objects, DocumentTemplateLocator locator) {
        super(objects, locator);
    }

    /**
     * Returns the report.
     *
     * @return the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template document can't be found
     */
    public IMReport<ObjectSet> getReport() {
        DocumentTemplate template = getTemplate();
        if (template == null) {
            String displayName = DescriptorHelper.getDisplayName(getShortName());
            throw new ReportException(NoTemplateForArchetype, displayName);
        }
        Document doc = getTemplateDocument();
        if (doc == null) {
            throw new DocumentException(NotFound);
        }
        ReportFactory factory = ServiceHelper.getBean(ReportFactory.class);
        return factory.createObjectSetReport(doc);
    }

}
