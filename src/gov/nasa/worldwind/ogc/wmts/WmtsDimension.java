/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

public class WmtsDimension extends OwsDescription {

    protected List<String> values = new ArrayList<>();

    public WmtsDimension(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public String getIdentifier() {
        return (String) (this.getField("Identifier") != null ? this.getField("Identifier") : this.getField("identifier"));
    }

    public String getUnitOfMeasure() {
        return (String) (this.getField("UOM") != null ? this.getField("UOM") : this.getField("uom"));
    }

    public String getUnitSymbol() {
        return (String) (this.getField("UnitSymbol") != null ? this.getField("UnitSymbol") : this.getField("unitSymbol"));
    }

    public String getValueDefault() {
        return (String) (this.getField("Default") != null ? this.getField("Default") : this.getField("default"));
    }

    public Boolean getCurrent() {
        return WWUtil.convertStringToBoolean((String) (this.getField("Current") != null ? this.getField("Current") : this.getField("current")));
    }

    public List<String> getValues() {
        return this.values;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {
        if (ctx.isStartElement(event, "Value")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s)) {
                this.values.add(s);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }

    }


}
