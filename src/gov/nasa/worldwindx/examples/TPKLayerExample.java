package gov.nasa.worldwindx.examples;

/**
 *
 * @author EagleEye
 */
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.mercator.tpk.TPKLayer;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;

/**
 * Created by Nebi Sarikaya on 22/08/22.
 */
public class TPKLayerExample extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private final TPKLayer layer;
        // private final TPKLayer layer2;

        public AppFrame() {
            super(true, true, false);
            this.layer = new TPKLayer("tpk/world-all.tpk", "World-TPK");
            //this.layer2 = new TPKLayer("Montreal_SEC", "Montreal Sectionals", "Montreal_SEC_20220714_cae.mbtiles");
            insertBeforePlacenames(getWwd(), this.layer);
            // insertBeforePlacenames(getWwd(), this.layer2);
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

        ApplicationTemplate.start("World TPKLayer", TPKLayerExample.AppFrame.class);
    }
}
