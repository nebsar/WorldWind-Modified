/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class OwsServiceContact extends AbstractXMLEventParser {

    public OwsServiceContact(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public String getIndividualName() {
        return (String) this.getField("IndividualName");
    }

    public String getPositionName() {
        return (String) this.getField("PositionName");
    }

    public String getRole() {
        return (String) this.getField("Role");
    }

    public OwsContactInfo getContactInfo() {
        return (OwsContactInfo) this.getField("ContactInfo");
    }

    @Override
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent event, Object... args) throws XMLStreamException {
        super.doParseEventAttributes(ctx, event, args);
    }

}
