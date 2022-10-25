package gov.nasa.worldwind.wmts;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.wms.Request;

import java.net.*;

/**
 * @author n.sarikaya
 * @version $Id: WMTSCapabilitiesRequest.java 1171 2022-10-25 17:21:52Z nsarikaya $
 */
public final class WMTSCapabilitiesRequest extends Request
{
    /** Construct an OGC GetCapabilities request using the default service. */
    public WMTSCapabilitiesRequest()
    {
    }

    /**
     * Constructs a request for the default service, WMTS, and a specified server.
     *
     * @param uri the address of the web service.
     *
     * @throws IllegalArgumentException if the uri is null.
     * @throws URISyntaxException       if the web service address is not a valid URI.
     */

    public WMTSCapabilitiesRequest(URI uri) throws URISyntaxException
    {
        super(uri, null);

        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Constructs a request for a specified service at a specified server.
     *
     * @param uri     the address of the web service.
     * @param service the service name. Common names are WMS, WMTS, WFS, WCS, etc.
     *
     * @throws IllegalArgumentException if the uri or service name is null.
     * @throws URISyntaxException       if the web service address is not a valid URI.
     */
    public WMTSCapabilitiesRequest(URI uri, String service) throws URISyntaxException
    {
        super(uri, service);

        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (service == null)
        {
            String message = Logging.getMessage("nullValue.WMSServiceNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
    }

    protected void initialize(String service)
    {
        super.initialize(service);
        this.setParam("REQUEST", "GetCapabilities");
        this.setParam("VERSION", "1.1.0");
    }
}
