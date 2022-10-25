package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
public class OwsWgs84BoundingBox extends OwsBoundingBox {

    public OwsWgs84BoundingBox(String namespaceURI) {
        super(namespaceURI);
    }

    public Sector getSector() {
        try {
            String[] lowerValues = this.getLowerCorner().split("\\s+");
            String[] upperValues = this.getUpperCorner().split("\\s+");

            double minLon = Double.parseDouble(lowerValues[0]);
            double minLat = Double.parseDouble(lowerValues[1]);
            double maxLon = Double.parseDouble(upperValues[0]);
            double maxLat = Double.parseDouble(upperValues[1]);

            return new Sector(Angle.fromDegrees(minLat),Angle.fromDegrees(maxLat),Angle.fromDegrees(minLon),Angle.fromDegrees(maxLon));
        } catch (Exception ex) {

            return null;
        }
    }
}
