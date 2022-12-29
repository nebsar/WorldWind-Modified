package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author n.sarikaya <nebsar@gmail.com>
 */
public class ObstacleGap extends AbstractMilStd2525TacticalGraphic {

    /**
     * First control point.
     */
    protected Position point1;
    /**
     * Second control point.
     */
    protected Position point2;
    /**
     * Third control point.
     */
    protected Position point3;
    /**
     * Fourth control point.
     */
    protected Position point4;

    /**
     * Path used to render the line.
     */
    protected ArrayList<Path> paths;

    public ObstacleGap(String sidc) {
        super(sidc);
    }

    /**
     * Indicates the graphics supported by this class.
     *
     * @return SIDC string that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics() {
        return Arrays.asList(TacGrpSidc.MOBSU_OBSTBP_CSGSTE_BRG,
                TacGrpSidc.MOBSU_OBSTBP_CSGSTE_ASTCA,
                TacGrpSidc.MOBSU_OBSTBP_CSGSTE);
    }

    /**
     * {@inheritDoc}
     */
    protected void doRenderGraphic(DrawContext dc) {
        for (Path path : this.paths) {
            path.render(dc);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void applyDelegateOwner(Object owner) {
        if (this.paths == null) {
            return;
        }

        for (Path path : this.paths) {
            path.setDelegateOwner(owner);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<? extends Position> getPositions() {
        return Arrays.asList(this.point1, this.point2, this.point3, this.point4);
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points that orient the graphic. Must provide at
     * least three points.
     */
    public void setPositions(Iterable<? extends Position> positions) {
        if (positions == null) {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try {
            Iterator<? extends Position> iterator = positions.iterator();
            this.point1 = iterator.next();
            this.point2 = iterator.next();
            this.point3 = iterator.next();
            this.point4 = iterator.next();
        } catch (NoSuchElementException e) {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.paths = null; // Need to recompute path for the new control points
    }

    /**
     * {@inheritDoc}
     */
    public Position getReferencePosition() {
        return this.point1;
    }

    /**
     * Compute positions and create the paths required to draw graphic.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void computeGeometry(DrawContext dc) {
        if (this.paths == null) {
            this.paths = new ArrayList<Path>();

            Angle angle = LatLon.greatCircleAzimuth(point2, point1);
            Angle angleAtPoint1 = angle.add(Angle.fromDegrees(45.0));
            Angle angleAtPoint2 = angle.add(Angle.fromDegrees(135.0));

            double radius = dc.getGlobe().getRadius();

            Angle edgeLength = Angle.fromRadians(2000.0 / radius);

            LatLon point1OffsetLocation = LatLon.greatCircleEndPosition(point1, angleAtPoint1, edgeLength);
            LatLon point2OffsetLocation = LatLon.greatCircleEndPosition(point2, angleAtPoint2, edgeLength);

            angle = LatLon.greatCircleAzimuth(point4, point3);
            Angle angleAtPoint3 = angle.subtract(Angle.fromDegrees(45.0));
            Angle angleAtPoint4 = angle.subtract(Angle.fromDegrees(135.0));

            LatLon point3OffsetLocation = LatLon.greatCircleEndPosition(point3, angleAtPoint3, edgeLength);
            LatLon point4OffsetLocation = LatLon.greatCircleEndPosition(point4, angleAtPoint4, edgeLength);

            // Create paths for the block
            String code = this.maskedSymbolCode;

            // Create the paths from point1 to point2
            this.paths.add(createPath(Arrays.asList(Position.fromDegrees(point1OffsetLocation.getLatitude().getDegrees(), point1OffsetLocation.getLongitude().getDegrees()),
                    this.point1, this.point2, Position.fromDegrees(point2OffsetLocation.getLatitude().getDegrees(), point2OffsetLocation.getLongitude().getDegrees()))));
            // Create the paths from centerPosition to point3
            this.paths.add(createPath(Arrays.asList(Position.fromDegrees(point3OffsetLocation.getLatitude().getDegrees(), point3OffsetLocation.getLongitude().getDegrees()),
                    this.point3, this.point4,
                    Position.fromDegrees(point4OffsetLocation.getLatitude().getDegrees(), point4OffsetLocation.getLongitude().getDegrees()))));

        }
        super.computeGeometry(dc);
    }

    /**
     * Create and configure the Path used to render this graphic.
     *
     * @param positions Positions that define the path.
     *
     * @return New path configured with defaults appropriate for this type of
     * graphic.
     */
    protected Path createPath(List<Position> positions) {
        Path path = new Path(positions);
        path.setFollowTerrain(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        path.setDelegateOwner(this.getActiveDelegateOwner());
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }

}
