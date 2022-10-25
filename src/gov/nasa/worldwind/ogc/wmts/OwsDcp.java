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

public class OwsDcp extends AbstractXMLEventParser {

    protected List<OwsHttpMethod> getMethods = new ArrayList<>();
    protected List<OwsHttpMethod> postMethods = new ArrayList<>();

    public OwsDcp(String namespaceURI) {
        super(namespaceURI);
    }

    public List<OwsHttpMethod> getGetMethods() {
        return this.getMethods;
    }

    public List<OwsHttpMethod> getPostMethods() {
        return this.postMethods;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {
        if (ctx.isStartElement(event, "HTTP")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OwsHttp) {
                    OwsHttp http = (OwsHttp) o;
                    this.getMethods.addAll(http.getGetMethods());
                    this.postMethods.addAll(http.getPostMethods());
                }
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
