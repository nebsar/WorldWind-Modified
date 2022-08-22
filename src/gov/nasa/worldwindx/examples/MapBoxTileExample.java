/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.mercator.mbtiles.MapBoxTileLayerNew;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;

/**
 * Modified by EagleEye on 01/08/22
 * Created by quonn on 07/06/16.
 */
public class MapBoxTileExample extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private final MapBoxTileLayerNew layer1;
        private final MapBoxTileLayerNew layer2;

        public AppFrame() {
            super(true, true, false);
            this.layer1 = new MapBoxTileLayerNew("Detroit_SEC", "Detroit Sectionals", "Detroit_SEC_20220714_ce1.mbtiles");
            this.layer2 = new MapBoxTileLayerNew("Montreal_SEC", "Montreal Sectionals", "Montreal_SEC_20220714_cae.mbtiles");
            insertBeforePlacenames(getWwd(), this.layer1);
            insertBeforePlacenames(getWwd(), this.layer2);
            getWwd().getView().setFieldOfView(Angle.fromDegrees(60.0));
            Dimension size = new Dimension(1200, 800);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }
    }

    public static void main(String[] args) {
        // Configure the initial view parameters so that the browser balloon is centered in the viewport.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 43);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -81);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 950000);
        Configuration.setValue(AVKey.INITIAL_PITCH, 0);

        ApplicationTemplate.start("World Wind MapBox", MapBoxTileExample.AppFrame.class);
    }
}
