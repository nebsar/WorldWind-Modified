package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.LatLon;

/**
 *
 * @author n.sarikaya <nebsar@gmail.com>
 */
public class PartialSurfaceCircle extends PartialSurfaceEllipse {


    public PartialSurfaceCircle(ShapeAttributes normalAttrs, LatLon center, double radius, double startAngle, double endAngle) {
        super(normalAttrs, center, radius, radius);
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    public PartialSurfaceCircle(LatLon center, double radius, double startAngle, double endAngle) {
        super(center, radius, radius);
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    public PartialSurfaceCircle(ShapeAttributes normalAttrs, LatLon center, double radius) {
        super(normalAttrs, center, radius, radius);
        this.startAngle = 0;
        this.endAngle = 360;
    }

    public PartialSurfaceCircle(LatLon center, double radius) {
        super(center, radius, radius);
        this.startAngle = 0;
        this.endAngle = 360;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public void setEndAngle(double endAngle) {
        this.endAngle = endAngle;
    }
}
