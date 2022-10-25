

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class WmtsTileMatrix extends OwsDescription {

//    protected String identifier;
//    protected String limitIdentifier;
//    protected double scaleDenominator;
//    protected String topLeftCorner;
//    protected int tileWidth;
//    protected int tileHeight;
//    protected int matrixWidth;
//    protected int matrixHeight;

    public WmtsTileMatrix(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public String getIdentifier() {
        return (String) (this.getField("Identifier") != null ? this.getField("Identifier") : this.getField("identifier"));
    }

    public double getScaleDenominator() {
        return WWUtil.convertStringToDouble((String) (this.getField("ScaleDenominator") != null ? this.getField("ScaleDenominator") : this.getField("scaleDenominator")));
    }

    public String getTopLeftCorner() {
        return (String) this.getField("TopLeftCorner");
    }

    public int getTileWidth() {
        return WWUtil.convertStringToInteger((String) this.getField("TileWidth"));
    }

    public int getTileHeight() {
        return WWUtil.convertStringToInteger((String) this.getField("TileHeight"));
    }

    public int getMatrixWidth() {
        return WWUtil.convertStringToInteger((String) this.getField("MatrixWidth"));
    }

    public int getMatrixHeight() {
        return WWUtil.convertStringToInteger((String) this.getField("MatrixHeight"));
    }
}
