/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

public class OwsAddress extends AbstractXMLEventParser {

    protected List<String> deliveryPoint = new ArrayList<>();
    protected List<String> postalCode = new ArrayList<>();

    protected List<String> country = new ArrayList<>();

    protected List<String> email = new ArrayList<>();

    public OwsAddress(String namespaceURI) {
        super(namespaceURI);
    }

    public List<String> getDeliveryPoints() {
        return this.deliveryPoint;
    }

    public String getCity() {
        return (String) (this.getField("City") != null ? this.getField("City") : this.getField("city"));
    }

    public String getAdministrativeArea() {
        return (String) (this.getField("AdministrativeArea") != null
                ? this.getField("AdministrativeArea") : this.getField("administrativeArea"));
    }

    public List<String> getPostalCodes() {
        return this.postalCode;
    }

    public List<String> getCountries() {
        return this.country;
    }

    public List<String> getElectronicMailAddresses() {
        return this.email;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {
        if (ctx.isStartElement(event, "DeliveryPoint") || ctx.isStartElement(event, "deliveryPoint")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.deliveryPoint.add(s);
        } else if (ctx.isStartElement(event, "PostalCode") || ctx.isStartElement(event, "postalCode")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.postalCode.add(s);
        } else if (ctx.isStartElement(event, "Country") || ctx.isStartElement(event, "country")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.country.add(s);
        } else if (ctx.isStartElement(event, "ElectronicMailAddress")
                || ctx.isStartElement(event, "electronicMailAddress")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.email.add(s);
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
