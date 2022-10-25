/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class OwsAllowedValues extends AbstractXMLEventParser {

    protected List<String> allowedValues = new ArrayList<>();

    public OwsAllowedValues(String namespaceURI) {
        super(namespaceURI);
    }

    public List<String> getValues() {
        return this.allowedValues;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {
        if (ctx.isStartElement(event, "Value")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.allowedValues.add(s);
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
