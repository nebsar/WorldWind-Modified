package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

public class OwsKeywords extends AbstractXMLEventParser {

    List<String> keywords = new ArrayList<>();

    public OwsKeywords(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public List<String> getKeywords() {
        return this.keywords;
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args) throws XMLStreamException {
        if (ctx.isStartElement(event, "Keyword")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.keywords.add(s);
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
