/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

public class WmtsTheme extends OwsDescription {

    protected String identifier;

    protected List<String> layerRefs = new ArrayList<>();

    public WmtsTheme(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public String getIdentifier() {
        return (String) this.getField("Identifier");
    }

    public List<String> getLayerRefs() {
        return this.layerRefs;
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
            throws XMLStreamException {
        if (ctx.isStartElement(event, "LayerRef")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.layerRefs.add(s);
        }  else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
