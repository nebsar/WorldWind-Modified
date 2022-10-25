/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParser;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

public class OwsOperationsMetadata extends AbstractXMLEventParser {

    protected List<OwsOperation> operations = new ArrayList<>();

    public OwsOperationsMetadata(String nameSpaceURI) {
        super(nameSpaceURI);
    }

    public List<OwsOperation> getOperations() {
        return this.operations;
    }

    public OwsOperation getGetCapabilities() {
        for (OwsOperation operation : this.operations) {
            if (operation.getName().equals("GetCapabilities")) {
                return operation;
            }
        }

        return null;
    }

    public OwsOperation getGetTile() {
        for (OwsOperation operation : this.operations) {
            if (operation.getName().equals("GetTile")) {
                return operation;
            }
        }

        return null;
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args) throws XMLStreamException {
        if (ctx.isStartElement(event, "Operation")) {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null) {
                 Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OwsOperation)
                    this.operations.add((OwsOperation) o);
            }
        } else {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
