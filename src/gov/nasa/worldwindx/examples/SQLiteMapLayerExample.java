/**
 *
 * @author EagleEye
 */
/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.mercator.sqlitemap.SQLiteMapLayer;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;

/**
 * Created by Nebi Sarikaya on 08/18/22.
 */
public class SQLiteMapLayerExample extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private final SQLiteMapLayer layer1;
       // private final SQLiteMapLayer layer2;

        public AppFrame() {
            super(true, true, false);
            this.layer1 = new SQLiteMapLayer("Toronto", "Toronto Sectionals", "5000_Toronto_2205_187_nomosaic.sqlite");
           // layer1.setOpacity(0.5);
         //   this.layer2 = new SQLiteMapLayer("Montreal", "Montreal Sectionals", "5002_Montreal_2205_187_nomosaic.sqlite");
           this.layer1.setUseMipMaps(true);
            insertBeforePlacenames(getWwd(), this.layer1);
            //getWwd().getView().setFieldOfView(Angle.fromDegrees(150));
         //   insertBeforePlacenames(getWwd(), this.layer2);
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

        ApplicationTemplate.start("World Wind SQLLite Map", SQLiteMapLayerExample.AppFrame.class);
    }
}
