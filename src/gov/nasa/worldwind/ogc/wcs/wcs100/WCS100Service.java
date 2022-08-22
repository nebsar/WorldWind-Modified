/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCS100Service.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100Service extends AbstractXMLEventParser
{
    protected List<String> accessConstraints = new ArrayList<String>(1);

    public WCS100Service(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getDescription()
    {
        return (String) this.getField("description");
    }

    public String getName()
    {
        return (String) this.getField("name");
    }

    public String getLabel()
    {
        return (String) this.getField("label");
    }

    public List<String> getAccessConstraints()
    {
        return this.accessConstraints;
    }

    public String getFees()
    {
        return (String) this.getField("fees");
    }

    public WCS100MetadataLink getMetadataLink()
    {
        return (WCS100MetadataLink) this.getField("metadataLink");
    }

    public List<String> getKeywords()
    {
        return ((StringListXMLEventParser) this.getField("keywords")).getStrings();
    }

    public WCS100ResponsibleParty getResponsibleParty()
    {
        return (WCS100ResponsibleParty) this.getField("responsibleParty");
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "accessConstraints"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.accessConstraints.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}