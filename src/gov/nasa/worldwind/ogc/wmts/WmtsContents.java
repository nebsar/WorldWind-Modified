package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

public class WmtsContents extends AbstractXMLEventParser {

    protected List<WmtsLayer> layers = new ArrayList<>();
    protected List<WmtsTileMatrixSet> tileMatrixSets = new ArrayList<>();

    public WmtsContents(String namespaceURI) {
        super(namespaceURI);
    }

    public List<WmtsLayer> getLayers() {
        return this.layers;
    }

    public List<WmtsTileMatrixSet> getTileMatrixSets() {
        return this.tileMatrixSets;
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {
        if (ctx.isStartElement(event, "Layer")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsLayer)
                    this.layers.add((WmtsLayer) o);
            }
        } else if (ctx.isStartElement(event, "TileMatrixSet")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsTileMatrixSet)
                    this.tileMatrixSets.add((WmtsTileMatrixSet) o);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}