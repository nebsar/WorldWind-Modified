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


public class WmtsTileMatrixSetLimits extends AbstractXMLEventParser {

    public WmtsTileMatrixSetLimits(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    protected List<WmtsTileMatrixLimits> tileMatrixLimits = new ArrayList<>();

    public List<WmtsTileMatrixLimits> getTileMatrixLimits() {
        return this.tileMatrixLimits;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {

        if (ctx.isStartElement(event, "TileMatrixLimits")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsTileMatrixLimits)
                    this.tileMatrixLimits.add((WmtsTileMatrixLimits) o);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }

}
