/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;

public class WmtsResourceUrl extends AbstractXMLEventParser {

    public WmtsResourceUrl(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    protected String format;
    protected String resourceType;

    protected String template;

    public String getFormat() {
        return this.format;
    }

    private void setFormat(String format) {
        this.format = format;
    }

    public String getResourceType() {
        return this.resourceType;
    }

    private void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getTemplate() {
        return this.template;
    }

    private void setTemplate(String template) {
        this.template = template;
    }

    @Override
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent event, Object... args) {
        Iterator iter = event.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext()) {
            Attribute attr = (Attribute) iter.next();
            if (attr.getName().getLocalPart().equals("format") && attr.getValue() != null) {
                this.setFormat(attr.getValue());
            } else if (attr.getName().getLocalPart().equals("resourceType") && attr.getValue() != null) {
                this.setResourceType(attr.getValue());
            } else if (attr.getName().getLocalPart().equals("template") && attr.getValue() != null) {
                this.setTemplate(attr.getValue());
            }
        }
    }
}
