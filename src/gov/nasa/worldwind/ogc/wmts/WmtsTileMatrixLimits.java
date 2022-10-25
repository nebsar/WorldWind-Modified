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

public class WmtsTileMatrixLimits extends AbstractXMLEventParser {

    protected String tileMatrixIdentifier;

    protected int minTileRow;

    protected int maxTileRow;

    protected int minTileCol;

    protected int maxTileCol;

    public WmtsTileMatrixLimits(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public String getTileMatrixIdentifier() {
        return (String) this.getField("TileMatrix");
    }

    public int getMinTileRow() {
        return WWUtil.convertStringToInteger((String) this.getField("MinTileRow"));
    }

    public int getMaxTileRow() {
        return WWUtil.convertStringToInteger((String) this.getField("MaxTileRow"));
    }

    public int getMinTileCol() {
        return WWUtil.convertStringToInteger((String) this.getField("MinTileCol"));
    }

    public int getMaxTileCol() {
        return WWUtil.convertStringToInteger((String) this.getField("MaxTileCol"));
    }

}
