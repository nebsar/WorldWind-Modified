
package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

public class WmtsStyle extends OwsDescription {

    protected String identifier;
    protected List<WmtsElementLink> legendUrls = new ArrayList<>();

    public WmtsStyle(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public String getIdentifier() {
        String s = (String) this.getField("Identifier");
        return s;
    }

    public boolean isDefault() {
        return WWUtil.convertStringToBoolean((String) this.getField("isDefault"));
    }

    public List<WmtsElementLink> getLegendUrls() {
        return this.legendUrls;
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args) throws XMLStreamException {
        if (ctx.isStartElement(event, "LegendURL")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WmtsElementLink)
                    this.legendUrls.add((WmtsElementLink) o);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }

    }
}
