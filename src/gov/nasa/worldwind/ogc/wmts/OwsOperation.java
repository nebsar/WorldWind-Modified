package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

public class OwsOperation extends AbstractXMLEventParser {

    protected List<OwsDcp> dcps = new ArrayList();

    public OwsOperation(String namespaceURI) {
        super(namespaceURI);
    }

    public String getName() {
        return (String) this.getField("name");
    }

    public List<OwsDcp> getDCPs() {
        return this.dcps;
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {
        if (ctx.isStartElement(event, "DCP")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OwsDcp)
                    this.dcps.add((OwsDcp) o);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
