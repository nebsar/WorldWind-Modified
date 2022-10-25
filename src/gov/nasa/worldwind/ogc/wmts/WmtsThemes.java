/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;


public class WmtsThemes extends AbstractXMLEventParser {

    protected List<WmtsTheme> themes = new ArrayList<>();

    public WmtsThemes(String nameSapceURI) {
        super(nameSapceURI);
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {

        if (ctx.isStartElement(event, "Theme")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsTheme)
                    this.themes.add((WmtsTheme) o);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }


}
