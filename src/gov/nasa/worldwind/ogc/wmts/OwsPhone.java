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

public class OwsPhone extends AbstractXMLEventParser {
    protected List<String> voice = new ArrayList<>();
    protected List<String> fax = new ArrayList<>();

    public OwsPhone(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public List<String> getVoice() {
        return this.voice;
    }

    public List<String> getFax() {
        return this.fax;
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args) throws XMLStreamException {
        if (ctx.isStartElement(event, "Voice")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.voice.add(s);
        } else if (ctx.isStartElement(event, "Facsimile")) {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.fax.add(s);
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
