/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.ogc.wcs.wcs100.WCS100MetadataLink;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;


public class WmtsLayer extends OwsDescription {

    protected String identifier;

    protected List<OwsBoundingBox> boundingBoxes = new ArrayList<>();
    protected OwsWgs84BoundingBox wgs84BoundingBox;
    protected List<WmtsElementLink> metadata = new ArrayList<>();
    protected List<WmtsStyle> styles = new ArrayList<>();
    protected List<String> formats = new ArrayList<>();
    protected List<String> infoFormats = new ArrayList<>();
    protected List<WmtsTileMatrixSetLink> tileMatrixSetLinks = new ArrayList<>();
    protected List<WmtsResourceUrl> resourceUrls = new ArrayList<>();
    protected List<WmtsDimension> dimensions = new ArrayList<>();

    public WmtsLayer(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public String getIdentifier() {
        return (String) this.getField("Identifier");
    }

    public List<WmtsDimension> getDimensions() {
        return this.dimensions;
    }

    public OwsWgs84BoundingBox getWgs84BoundingBox() {
        return (OwsWgs84BoundingBox) this.getField("WGS84BoundingBox");
    }

    public List<OwsBoundingBox> getBoundingBoxes() {
        return this.boundingBoxes;
    }

    public List<WmtsElementLink> getMetadata() {
        return this.metadata;
    }

    public List<WmtsStyle> getStyles() {
        return this.styles;
    }

    public List<String> getFormats() {
        return this.formats;
    }

    public List<String> getInfoFormats() {
        return this.infoFormats;
    }

    public List<WmtsTileMatrixSetLink> getTileMatrixSetLinks() {
        return this.tileMatrixSetLinks;
    }

    public List<WmtsResourceUrl> getResourceUrls() {
        return this.resourceUrls;
    }

    public WMTS100Capabilities getCapabilities() {
        XMLEventParser parent = this.getParent();
        while (parent != null) {
            if (parent instanceof WMTS100Capabilities) {
                return (WMTS100Capabilities) parent;
            }
            parent = parent.getParent();
        }

        return null;
    }

    public List<WmtsTileMatrixSet> getLayerSupportedTileMatrixSets() {
        List<WmtsTileMatrixSet> associatedTileMatrixSets = new ArrayList<>();
        for (WmtsTileMatrixSetLink tileMatrixSetLink : this.getTileMatrixSetLinks()) {
            for (WmtsTileMatrixSet tileMatrixSet : this.getCapabilities().getTileMatrixSets()) {
                if (tileMatrixSet.getIdentifier().equals(tileMatrixSetLink.getIdentifier())) {
                    associatedTileMatrixSets.add(tileMatrixSet);
                }
            }
        }

        return associatedTileMatrixSets;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {

        if (ctx.isStartElement(event, "BoundingBox")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OwsBoundingBox)
                    this.boundingBoxes.add((OwsBoundingBox) o);
            }
        } else if (ctx.isStartElement(event, "Metadata")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsElementLink)
                    this.metadata.add((WmtsElementLink) o);
            }
        } else if (ctx.isStartElement(event, "Style")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsStyle)
                    this.styles.add((WmtsStyle) o);
            }
        } else if (ctx.isStartElement(event, "Format")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.formats.add(s);
        } else if (ctx.isStartElement(event, "InfoFormat")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.infoFormats.add(s);
        } else if (ctx.isStartElement(event, "ResourceURL")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsResourceUrl)
                    this.resourceUrls.add((WmtsResourceUrl) o);
            }
        } else if (ctx.isStartElement(event, "TileMatrixSetLink")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsTileMatrixSetLink) {
                    this.tileMatrixSetLinks.add((WmtsTileMatrixSetLink) o);
                }
            }
        } else if (ctx.isStartElement(event, "Dimension")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsDimension)
                    this.dimensions.add((WmtsDimension) o);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
