package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class OwsBoundingBox extends AbstractXMLEventParser {

       public OwsBoundingBox(String namespaceURI) {
        super(namespaceURI);
    }

    public String getLowerCorner() {
        return (String) (this.getField("lowerCorner") != null ? this.getField("lowerCorner") : this.getField("LowerCorner"));
    }

    public String getUpperCorner() {
        return (String) (this.getField("upperCorner") != null ? this.getField("upperCorner") : this.getField("UpperCorner"));
    }

}
