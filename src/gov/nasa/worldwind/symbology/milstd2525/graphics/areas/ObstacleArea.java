/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.*;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Obstacle Free Area graphic (2.X.3.1.1.4).
 *
 * @author n.sarikaya
 * @version $Id: ObstacleFreeArea.java 545 2022-12-20 16:99:21Z n.sarikaya $
 */
public class ObstacleArea extends BasicArea {

    /**
     * Path to the image used for the polygon fill pattern.
     */
    protected static final String DIAGONAL_FILL_PATH = "images/diagonal-fill-16x16.png";

    /**
     * Default number of wave lengths for a simple shape. This number is used to
     * compute a default wave length.
     */
    public static final int DEFAULT_NUM_WAVES = 100;

    /**
     * Original positions specified by the application.
     */
    protected Iterable<? extends Position> positions;
    /**
     * Positions computed from the original positions. This list includes the
     * positions necessary to draw the triangle wave.
     */
    protected List<Position> computedPositions;

    /**
     * Indicates the wavelength of the triangle wave that forms the graphic's
     * border.
     */
    protected double waveLength;

    /**
     * The polygon with triangle wave.
     */
    //protected SurfacePolygon wavePolygon = new SurfacePolygon();
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this
     * class supports.
     */
    public static List<String> getSupportedGraphics() {
        return Arrays.asList(TacGrpSidc.MOBSU_OBST_GNL_OFA,
                TacGrpSidc.MOBSU_OBST_GNL_ORA,
                TacGrpSidc.MOBSU_OBST_GNL_Z);
    }

    /**
     * Create a new graphic.
     *
     * @param sidc MIL-STD-2525C identifier code.
     */
    public ObstacleArea(String sidc) {
        super(sidc);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPositions(Iterable<? extends Position> positions) {
        this.positions = positions;
        this.computedPositions = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<? extends Position> getPositions() {
        return this.positions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveTo(Position position) {
        if (position == null) {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position oldPosition = this.getReferencePosition();

        // The reference position is null if this shape has no positions. In this case moving the shape to a new
        // reference position is meaningless. Therefore we fail softly by exiting and doing nothing.
        if (oldPosition == null) {
            return;
        }

        this.positions = Position.computeShiftedPositions(oldPosition, position, this.getPositions());

        // Just move the polygon instead of recomputing the triangle wave using the shifted position list. This makes
        // moving the polygon using a dragger smoother.
        this.polygon.moveTo(position);

    }

    /**
     * Indicates the wavelength of the triangle wave that forms the graphic's
     * boundary. This is the length from the start of one "tooth" to the start
     * of the next.
     * <pre>
     * __/\__/\__
     *  ^   ^
     *   Wavelength
     * </pre>
     *
     * @return The wave length, in meters.
     */
    public double getWaveLength() {
        return this.waveLength;
    }

    /**
     * Specifies the wavelength of the triangle wave that forms the graphic's
     * boundary. See {@link #getWaveLength()} for more information on how this
     * distance is interpreted.
     *
     * @param waveLength The wavelength, in meters.
     */
    public void setWaveLength(double waveLength) {
        this.waveLength = waveLength;
    }

    /**
     * {@inheritDoc}
     */
    public void preRender(DrawContext dc) {
        if (!this.isVisible()) {
            return;
        }

        super.preRender(dc);

    }

    @Override
    protected void doRenderGraphic(DrawContext dc) {
        super.doRenderGraphic(dc);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void computeGeometry(DrawContext dc) {
        if (this.computedPositions == null && this.positions != null) {
            this.generateIntermediatePositions(dc, this.positions);
            this.polygon.setLocations(this.computedPositions);

        }
        super.computeGeometry(dc);
    }

    /**
     * Generate the positions required to draw the polygon with a triangle wave
     * boundary.
     *
     * @param dc Current draw context.
     * @param positions Positions that define the polygon boundary.
     */
    protected void generateIntermediatePositions(DrawContext dc, Iterable<? extends Position> positions) {
        Globe globe = dc.getGlobe();
        List<Position> wavePositions = new ArrayList<Position>();

        double waveLength = this.getWaveLength();
        if (waveLength == 0) {
            waveLength = 2500;
        }
        double amplitude = waveLength / 2.0;

        TriangleWavePositionIterator iterator;
        if (TacGrpSidc.MOBSU_OBST_GNL_Z.equals(maskedSymbolCode)) {
            iterator = new TriangleWavePositionIterator(positions, waveLength, amplitude,
                    globe, false);
        } else {
            iterator = new TriangleWavePositionIterator(positions, waveLength, amplitude,
                    globe, true);
        }

        while (iterator.hasNext()) {
            wavePositions.add(iterator.next());
        }

        this.computedPositions = wavePositions;
    }

    /**
     * Compute a default tooth size for the polygon. This method computes the
     * default wavelength as a function of the size of the polygon and the
     * number of vertices. As the polygon gets bigger the wavelength will
     * increase, but as the number of the vertices increases the wavelength will
     * decrease to prevent the waves from obscuring the shape of the polygon.
     *
     * @param globe Globe on which the area will be rendered.
     *
     * @return The wave length.
     */
    // TODO: this algorithm assumes that more control points means that the shape is more complicated, so the teeth
    // TODO: need to be smaller. But this isn't always the case; adding points to increase the resolution of a circle
    // TODO: shouldn't make the teeth smaller.
    protected double computeDefaultWavelength(Globe globe) {
        double perimeter = 0;
        int count = 0;

        // Compute the number of vertices and the perimeter of the polygon.
        Position first = null;
        Position prev = null;

        for (Position pos : this.positions) {
            if (prev != null) {
                Angle dist = LatLon.greatCircleDistance(pos, prev);
                perimeter += dist.radians;
            } else {
                first = pos;
            }

            prev = pos;
            count += 1;
        }

        // If the polygon is not closed account for the missing segment.
        if (prev != null && !prev.equals(first)) {
            Angle dist = LatLon.greatCircleDistance(first, prev);
            perimeter += dist.radians;
        }

        // Compute the "complexity" of the polygon. A triangle has complexity of one, and complexity increases as
        // vertices are added.
        double complexity = Math.sqrt(count / 3.0);

        perimeter = perimeter * globe.getRadius(); // Convert perimeter to meters.

        double numwaves = perimeter * DEFAULT_NUM_WAVES / 45388.37616672155;

        return perimeter / (complexity * DEFAULT_NUM_WAVES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyDefaultAttributes(ShapeAttributes attributes) {
        super.applyDefaultAttributes(attributes);

        if (TacGrpSidc.MOBSU_OBST_GNL_ORA.equals(maskedSymbolCode)) {
            // Enable the polygon interior and set the image source to draw a fill pattern of diagonal lines.
            attributes.setDrawInterior(true);
            attributes.setImageSource(this.getImageSource());
        }
    }

    /**
     * Indicates the source of the image that provides the polygon fill pattern.
     *
     * @return The source of the polygon fill pattern.
     */
    protected Object getImageSource() {
        return DIAGONAL_FILL_PATH;
    }

}
