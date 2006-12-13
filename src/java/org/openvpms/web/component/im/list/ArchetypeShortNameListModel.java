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

package org.openvpms.web.component.im.list;

import java.util.Arrays;
import java.util.List;

import nextapp.echo2.app.list.AbstractListModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Archetype short name list model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeShortNameListModel extends AbstractListModel {

    /**
     * Short name indicating that all values apply.
     */
    public static final String ALL = "all";

    /**
     * Localised displlay name for "all".
     */
    private final String ALL_LOCALISED = Messages.get("list.all");

    /**
     * The short names. The first column is the short name, the second the
     * corresponding display name.
     */
    private final String[][] _shortNames;

    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(ArchetypeShortNameListModel.class);

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     */
    public ArchetypeShortNameListModel(String[] shortNames) {
        this(shortNames, false);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code>, add a localised "All"
     */
    public ArchetypeShortNameListModel(String[] shortNames, boolean all) {
        this(shortNames, all, ServiceHelper.getArchetypeService(), true);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code>, add a localised "All"
     */
    public ArchetypeShortNameListModel(List<String> shortNames, boolean all) {
        this(shortNames.toArray(new String[0]), all,
                ServiceHelper.getArchetypeService(), true);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code>, add a localised "All"
     * @param sort       if <code>true</code>, sort the list alphabetically
     */
    public ArchetypeShortNameListModel(List<String> shortNames, boolean all, boolean sort) {
        this(shortNames.toArray(new String[0]), all,
                ServiceHelper.getArchetypeService(), sort);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code>, add a localised "All"
     * @param sort       if <code>true</code>, sort the list alphabetically
     */
    public ArchetypeShortNameListModel(String[] shortNames, boolean all, boolean sort) {
        this(shortNames, all,
                ServiceHelper.getArchetypeService(), sort);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code> add a localised "All"
     * @param service    the archetype service
     * @param sort TODO
     */
    public ArchetypeShortNameListModel(String[] shortNames,
                                       boolean all,
                                       IArchetypeService service, boolean sort) {
        if (sort) {
        	Arrays.sort(shortNames);        	
        }
        int size = shortNames.length;
        int index = 0;
        if (all) {
            ++size;
        }
        _shortNames = new String[size][2];
        if (all) {
            _shortNames[index][0] = ALL;
            _shortNames[index][1] = ALL_LOCALISED;
            ++index;
        }
        for (int i = 0; i < shortNames.length; ++i, ++index) {
            String shortName = shortNames[i];
            _shortNames[index][0] = shortName;
            ArchetypeDescriptor descriptor
                    = service.getArchetypeDescriptor(shortName);
            String displayName = null;
            if (descriptor != null) {
                displayName = descriptor.getDisplayName();
            } else {
                _log.error("No archetype descriptor for shortname=" + shortName);
            }
            if (StringUtils.isEmpty(displayName)) {
                displayName = shortName;
            }
            _shortNames[index][1] = displayName;
        }
    }

    /**
     * Returns the value at the specified index in the list.
     *
     * @param index the index
     * @return the value
     */
    public Object get(int index) {
        return _shortNames[index][1];
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    public int size() {
        return _shortNames.length;
    }

    /**
     * Returns the short name at the specified index in the list.
     *
     * @param index the index
     * @return the short name
     */
    public String getShortName(int index) {
        return _shortNames[index][0];
    }

    /**
     * Returns the index of the specified short name.
     *
     * @param shortName the short name
     * @return the index of <code>shortName</code>, or <code>-1</code> if it
     *         doesn't exist
     */
    public int indexOf(String shortName) {
        int result = -1;
        for (int i = 0; i < _shortNames.length; ++i) {
            if (_shortNames[i][0].equals(shortName)) {
                result = i;
                break;
            }
        }
        return result;
    }
    private static class ArchetypeShortNamesComparable implements Comparable {

		public int compareTo(Object arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
    	
    }
}
