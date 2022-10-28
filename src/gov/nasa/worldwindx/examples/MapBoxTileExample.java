/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.Earth.WMTS3857Layer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.mercator.mbtiles.MapBoxTileLayerNew;
import gov.nasa.worldwind.ogc.wmts.WMTS100Capabilities;
import gov.nasa.worldwind.ogc.wmts.WMTSLayerFactory;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.wmts.WMTSImageLayer;

import java.awt.*;
import java.net.URI;

/**
 * Modified by EagleEye on 01/08/22 Created by quonn on 07/06/16.
 */
public class MapBoxTileExample extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private final MapBoxTileLayerNew layer1;
        private final MapBoxTileLayerNew layer2;

        private final Layer layer3;

        public AppFrame() {
            super(true, true, false);
            this.layer1 = new MapBoxTileLayerNew("Detroit_SEC", "Detroit Sectionals", "Detroit_SEC_20220714_ce1.mbtiles");
            this.layer2 = new MapBoxTileLayerNew("Montreal_SEC", "Montreal Sectionals", "Montreal_SEC_20220714_cae.mbtiles");

            this.layer3 = WMTSLayerFactory.createWMTSImageLayer("https://tiles.geoservice.dlr.de/service/wmts", "EOC Basemap");

            Layer wmts3857Layer = WMTSLayerFactory.createWMTSImageLayer("https://server.arcgisonline.com/arcgis/rest/services/NatGeo_World_Map/MapServer/WMTS/1.0.0/WMTSCapabilities.xml", "NatGeo_World_Map");

            insertBeforePlacenames(getWwd(), this.layer1);
            insertBeforePlacenames(getWwd(), this.layer2);
            insertBeforePlacenames(getWwd(), this.layer3);
            insertBeforePlacenames(getWwd(), wmts3857Layer);
            //layer3.setEnabled(true);
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
