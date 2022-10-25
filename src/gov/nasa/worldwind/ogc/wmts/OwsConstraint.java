/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class OwsConstraint extends AbstractXMLEventParser {

    protected String name;
    protected List<String> allowedValues = new ArrayList<>();

    public OwsConstraint(String namespaceURI) {
        super(namespaceURI);
    }

    public String getName() {
        return (String) (this.getField("Name") != null ? this.getField("Name") : this.getField("name"));
    }

    public List<String> getAllowedValues() {
        return this.allowedValues;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {
        if (ctx.isStartElement(event, "AllowedValues")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OwsAllowedValues)
                    this.allowedValues.addAll(((OwsAllowedValues)  o).getValues());
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
