/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.ogc.ows.OWSAllowedValues;
import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class OwsHttp extends AbstractXMLEventParser {

    protected List<OwsHttpMethod> get = new ArrayList<>();
    protected List<OwsHttpMethod> post = new ArrayList<>();

    public OwsHttp(String namespaceURI) {
        super(namespaceURI);
    }

    public List<OwsHttpMethod> getGetMethods() {
        return this.get;
    }

    public List<OwsHttpMethod> getPostMethods() {
        return this.post;
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {
        if (ctx.isStartElement(event, "Get")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OwsHttpMethod)
                    this.get.add((OwsHttpMethod) o);
            }
        } else if (ctx.isStartElement(event, "Post")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OwsHttpMethod)
                    this.post.add((OwsHttpMethod) o);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
