package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.List;

public class PartialSurfaceEllipse extends SurfaceEllipse {

    protected double startAngle;
    protected double endAngle;

    public PartialSurfaceEllipse(ShapeAttributes normalAttrs, LatLon center, double majorRadius, double minorRadius, double startAngle, double endAngle) {
        super(normalAttrs, center, majorRadius, minorRadius);
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    public PartialSurfaceEllipse(LatLon center, double majorRadius, double minorRadius, double startAngle, double endAngle) {
        super(center, majorRadius, minorRadius);
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    public PartialSurfaceEllipse(ShapeAttributes normalAttrs, LatLon center, double majorRadius, double minorRadius) {
        super(normalAttrs, center, majorRadius, minorRadius);
        this.startAngle = 0;
        this.endAngle = 360;
    }

    public PartialSurfaceEllipse(LatLon center, double majorRadius, double minorRadius) {
        super(center, majorRadius, minorRadius);
        this.startAngle = 0;
        this.endAngle = 360;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public void setEndAngle(double endAngle) {
        this.endAngle = endAngle;
    }

    protected List<LatLon> computeLocations(Globe globe, int intervals) {
        if (globe == null) {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.majorRadius == 0 && this.minorRadius == 0) {
            return null;
        }

        double globeRadius = globe.getRadiusAt(this.center.getLatitude(), this.center.getLongitude());

        double sAngle = startAngle;
        List<LatLon> locations = new ArrayList<>();

        while (sAngle <= this.endAngle) {
            double yLength = this.majorRadius * Math.cos(Math.toRadians(sAngle));
            double xLength = this.minorRadius * Math.sin(Math.toRadians(sAngle));
            double distance = Math.sqrt(xLength * xLength + yLength * yLength);
            // azimuth runs positive clockwise from north and through 360 degrees.
            double azimuth = (Math.PI / 2.0) - (Math.acos(xLength / distance) * Math.signum(yLength)
                    - this.heading.radians);

            locations.add(LatLon.greatCircleEndPosition(this.center, azimuth, distance / globeRadius));

            if (this.endAngle == sAngle) {
                break;
            } else if (this.endAngle - sAngle < 5.0) {
                sAngle = endAngle;
            } else {
                sAngle += 5.0;
            }

        }

        return locations;
    }
}
