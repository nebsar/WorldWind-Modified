/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;


import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class WmtsElementLink extends AbstractXMLEventParser {

    protected String url;
    protected String format;

    public WmtsElementLink(String nameSpaceURI){
        super(nameSpaceURI);
    }

    public String getUrl() {
        return (String) this.getField("href");
    }

    public String getFormat() {
        return (String) this.getField("Format");
    }

    @Override
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent event, Object... args) throws XMLStreamException {
        super.doParseEventAttributes(ctx, event, args);
    }
}
