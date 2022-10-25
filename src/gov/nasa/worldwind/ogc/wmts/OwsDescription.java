
package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.StringListXMLEventParser;

import java.util.List;


public class OwsDescription extends AbstractXMLEventParser {

    public OwsDescription(String namespaceURI) {
        super(namespaceURI);
    }

    public String getTitle() {
        return (String) (this.getField("Title") != null ? this.getField("Title") : this.getField("title"));
    }

    public String getAbstract() {
        return (String) (this.getField("Abstract") != null ? this.getField("Abstract") : this.getField("abstract"));
    }

    public List<String> getKeywords() {
        return ((StringListXMLEventParser) this.getField("Keywords")).getStrings();
    }

}
