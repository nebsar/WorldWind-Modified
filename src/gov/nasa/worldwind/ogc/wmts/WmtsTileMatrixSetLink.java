/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;


import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class WmtsTileMatrixSetLink extends AbstractXMLEventParser {

    private String tileMatrixSetIdentifier;

    public WmtsTileMatrixSetLink(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public String getIdentifier() {
        return tileMatrixSetIdentifier;
    }

    private void setIdentifier(String identifier) {
        this.tileMatrixSetIdentifier = identifier;
    }

    public WmtsTileMatrixSetLimits getTileMatrixSetLimits() {
        return (WmtsTileMatrixSetLimits) (this.getField("TileMatrixSetLimits") != null ? this.getField("TileMatrixSetLimits") : this.getField("tileMatrixSetLimits"));
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args) throws XMLStreamException {
        if (ctx.isStartElement(event, "TileMatrixSet")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.setIdentifier(s);
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
