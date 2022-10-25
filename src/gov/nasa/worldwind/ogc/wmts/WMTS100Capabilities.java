package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.*;
import gov.nasa.worldwind.wmts.WMTSCapabilitiesRequest;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author n.sarikaya
 * @version $Id: WMTS100Capabilities.java  2022-10-05 10:47:25Z n.sarikaya$
 */
public class WMTS100Capabilities extends AbstractXMLEventParser {
    protected XMLEventReader eventReader;
    protected XMLEventParserContext parserContext;

    protected String owsNamespace = "http://www.opengis.net/ows/1.1";

    protected String wmtsNamespace = "http://www.opengis.net/wmts/1.0";

    protected String version;

    protected String updateSequence;

    protected OwsServiceIdentification serviceIdentification;

    protected OwsServiceProvider serviceProvider;

    protected OwsOperationsMetadata operationsMetadata;

    protected WmtsContents contents;

    protected List<WmtsTheme> themes = new ArrayList<>();

    protected List<WmtsElementLink> serviceMetadataUrls = new ArrayList<>();

    /**
     * Retrieves the WCS capabilities document from a specified WCS server.
     *
     * @param uri The URI of the server.
     * @return The WCS capabilities document for the specified server.
     * @throws java.lang.Exception                             if a general error occurs.
     * @throws IllegalArgumentException                        if the specified URI is invalid.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException if an error occurs retrieving the document.
     */
    public static WMTS100Capabilities retrieve(URI uri) throws Exception {
        try {
            WMTSCapabilitiesRequest request = new WMTSCapabilitiesRequest(uri, "WMTS");

            return new WMTS100Capabilities(request.toString());
        } catch (URISyntaxException e) {
            String message = Logging.getMessage("OGC.GetCapabilitiesURIInvalid", uri);
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }
    }

    public WMTS100Capabilities(Object docSource) {
        super(OGCConstants.WMTS_1_0_0_NAMESPACE_URI);

        this.eventReader = this.createReader(docSource);

        this.initialize();
    }

    protected void initialize() {
        this.parserContext = this.createParserContext(this.eventReader);
    }

    protected XMLEventReader createReader(Object docSource) {
        return WWXML.openEventReader(docSource);
    }

    protected XMLEventParserContext createParserContext(XMLEventReader reader) {
        this.parserContext = new BasicXMLEventParserContext(reader);
        this.parserContext.setDefaultNamespaceURI(this.getNamespaceURI());

        return this.parserContext;
    }

    public XMLEventParserContext getParserContext() {
        return this.parserContext;
    }

    /**
     * Returns the document's version number.
     *
     * @return the document's version number.
     */
    public String getVersion() {
        return (String) this.getField("version");
    }

    /**
     * Returns the document's update sequence.
     *
     * @return the document's update sequence.
     */

    public OwsServiceProvider getServiceProvider() {
        return (OwsServiceProvider) this.getField("ServiceProvider");
    }

    public WMTS100Capabilities getCapability() {
        return (WMTS100Capabilities) this.getField("Capability");
    }

    public OwsOperationsMetadata getOperationsMetadata() {
        return (OwsOperationsMetadata) this.getField("OperationsMetadata");
    }

    /**
     * Starts document parsing. This method initiates parsing of the XML document and returns when the full capabilities
     * document has been parsed.
     *
     * @param args optional arguments to pass to parsers of sub-elements.
     * @return <code>this</code> if parsing is successful, otherwise  null.
     * @throws javax.xml.stream.XMLStreamException if an exception occurs while attempting to read the event stream.
     */
    public WMTS100Capabilities parse(Object... args) throws XMLStreamException {
        XMLEventParserContext ctx = this.parserContext;

        QName capsName = new QName(this.getNamespaceURI(), "Capabilities");

        for (XMLEvent event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent()) {
            if (event == null)
                continue;

            if (event.isStartElement() && event.asStartElement().getName().equals(capsName)) {
                // Parse the attributes in order to get the version number.
                this.doParseEventAttributes(ctx, event);
                ctx.setDefaultNamespaceURI(this.getNamespaceURI());

                // Now register the parsers.
                this.registerParsers(ctx);

                super.parse(ctx, event, args);

                return this;
            }
        }

        return null;
    }

    public WmtsContents getContents() {
        return (WmtsContents) this.getField("Contents");
    }

    protected List<WmtsTileMatrixSet> getTileMatrixSets() {
        return getContents().getTileMatrixSets();
    }

    public List<String> getLayerNames() {
        List<String> names = new ArrayList<>();
        List<WmtsLayer> layers = getContents().getLayers();
        for (WmtsLayer layer : layers) {
            names.add(layer.getTitle());
        }

        return names;
    }

    public WmtsLayer getLayerByName(String name) {
        if (WWUtil.isEmpty(name))
            return null;

        List<WmtsLayer> layers = this.getContents().getLayers();

        for (WmtsLayer layer : layers) {
            if (layer.getTitle() != null && layer.getTitle().equals(name))
                return layer;
        }

        return null;
    }

    public WmtsTileMatrixSet getTileMatrixSet(String identifier) {
        for (WmtsTileMatrixSet tileMatrixSet : this.getContents().getTileMatrixSets()) {
            if (tileMatrixSet.getIdentifier().equals(identifier)) {
                return tileMatrixSet;
            }
        }

        return null;
    }

    protected void registerParsers(XMLEventParserContext ctx) {

        ctx.addStringParsers(OGCConstants.OWS_1_1_0_NAMESPACE_URI, new String[]
                {
                        "Abstract",
                        "AccessConstraints",
                        "AdministrativeArea",
                        "City",
                        "Country",
                        "DeliveryPoint",
                        "ElectronicMailAddress",
                        "Facsimile",
                        "Fees",
                        "Identifier",
                        "IndividualName",
                        "Keyword",
                        "LowerCorner",
                        "PositionName",
                        "PostalCode",
                        "ProviderName",
                        "ServiceType",
                        "ServiceTypeVersion",
                        "SupportedCRS",
                        "Title",
                        "UOM",
                        "UpperCorner",
                        "Value",
                        "Voice",
                });


        ctx.addStringParsers(OGCConstants.OWS_1_1_0_NAMESPACE_URI, new String[]
                {
                        "Current",
                        "Default",
                        "InfoFormat",
                        "LayerRef",
                        "MaxTileCol",
                        "MaxTileRow",
                        "MinTileCol",
                        "MinTileRow",
                        "Profile",
                        "UnitSymbol",
                        "UOM",
                        "Value",

                });

        ctx.addStringParsers(this.getNamespaceURI(), new String[]
                {
                        "Format",
                        "MatrixHeight",
                        "MatrixWidth",
                        "ScaleDenominator",
                        "TopLeftCorner",
                        "TileHeight",
                        "TileMatrixSet",
                        "TileWidth",
                        "WellKnownScaleSet"});

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Address"),
                new OwsAddress(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "AllowedValues"),
                new OwsAllowedValues(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "BoundingBox"),
                new OwsBoundingBox(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "Capabilities"),
                new WMTS100Capabilities(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Constraint"),
                new OwsConstraint(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "ContactInfo"),
                new OwsContactInfo(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "Contents"),
                new WmtsContents(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "DCP"),
                new OwsDcp(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Dimension"),
                new WmtsDimension(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Get"),
                new OwsHttpMethod(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "HTTP"),
                new OwsHttp(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Keywords"),
                new OwsKeywords(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "Layer"),
                new WmtsLayer(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "LegendURL"),
                new WmtsElementLink(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Metadata"),
                new WmtsElementLink(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Operation"),
                new OwsOperation(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "OperationsMetadata"),
                new OwsOperationsMetadata(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Phone"),
                new OwsPhone(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Post"),
                new OwsHttpMethod(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "ProviderSite"),
                new AttributesOnlyXMLEventParser(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "ResourceURL"),
                new WmtsResourceUrl(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "ServiceContact"),
                new OwsServiceContact(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "ServiceIdentification"),
                new OwsServiceIdentification(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "ServiceMetadataURL"),
                new WmtsElementLink(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "ServiceProvider"),
                new OwsServiceProvider(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "Style"),
                new WmtsStyle(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Theme"),
                new WmtsTheme(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "Themes"),
                new WmtsThemes(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "TileMatrix"),
                new WmtsTileMatrix(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "TileMatrixLimits"),
                new WmtsTileMatrixLimits(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "TileMatrixSet"),
                new WmtsTileMatrixSet(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "TileMatrixSetLimits"),
                new WmtsTileMatrixSetLimits(OGCConstants.OWS_1_1_0_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "TileMatrixSetLink"),
                new WmtsTileMatrixSetLink(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.OWS_1_1_0_NAMESPACE_URI, "WGS84BoundingBox"),
                new OwsWgs84BoundingBox(OGCConstants.OWS_1_1_0_NAMESPACE_URI));
    }
}
