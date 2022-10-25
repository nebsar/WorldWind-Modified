
package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class OwsHttpMethod extends AbstractXMLEventParser {

    protected List<OwsConstraint> constraints = new ArrayList<>();

    public OwsHttpMethod(String namespaceURI) {
        super(namespaceURI);
    }

    public String getUrl() {
        return (String) (this.getField("href"));
    }

    public List<OwsConstraint> getConstraints() {
        return this.constraints;
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {
        if (ctx.isStartElement(event, "Constraint")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OwsConstraint)
                    this.constraints.add((OwsConstraint) o);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
