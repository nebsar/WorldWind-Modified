package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.AttributesOnlyXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class OwsContactInfo extends AbstractXMLEventParser {

    protected OwsPhone phone;
    protected OwsAddress address;

    public OwsContactInfo(String namespaceURI) {
        super(namespaceURI);
    }

    public OwsPhone getPhone() {
        return (OwsPhone) (this.getField("Phone") != null ? this.getField("Phone") : this.getField("phone"));
    }

    public OwsAddress getAddress() {
        return (OwsAddress) (this.getField("Address") != null ? this.getField("Address") : this.getField("address"));
    }

    public String getOnlineResource() {
        AttributesOnlyXMLEventParser parser = (AttributesOnlyXMLEventParser)
                (this.getField("OnlineResource") != null ? this.getField("OnlineResource")
                        : this.getField("onlineResource"));

        return parser != null ? (String) parser.getField("href") : null;
    }

    @Override
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent event, Object... args) throws XMLStreamException {
        super.doParseEventAttributes(ctx, event, args);
    }
}
