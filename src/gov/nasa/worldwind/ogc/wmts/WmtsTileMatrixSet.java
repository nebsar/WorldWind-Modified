package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

public class WmtsTileMatrixSet extends OwsDescription {

    protected List<WmtsTileMatrix> tileMatrices = new ArrayList<>();

    public WmtsTileMatrixSet(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public String getIdentifier() {
        return (String) this.getField("Identifier");
    }

    public String getSupportedCrs() {
        return (String) this.getField("SupportedCRS");
    }

    public String getWellKnownScaleSet() {
        return (String) this.getField("WellKnownScaleSet");
    }

    public OwsBoundingBox getBoundingBox() {
        return (OwsBoundingBox) this.getField("BoundingBox");
    }

    public List<WmtsTileMatrix> getTileMatrices() {
        return this.tileMatrices;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {

        if (ctx.isStartElement(event, "TileMatrix")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsTileMatrix)
                    this.tileMatrices.add((WmtsTileMatrix) o);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }

    }
}
