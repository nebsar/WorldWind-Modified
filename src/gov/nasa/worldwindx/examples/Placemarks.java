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
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.*;
import gov.nasa.worldwind.util.PlacemarkClutterFilter;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.util.ScreenSelector;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Illustrates how to use {@link gov.nasa.worldwind.render.PointPlacemark}. Also
 * shows how to use a 2525 tactical symbol as a placemark image.
 *
 * @author tag
 * @version $Id: Placemarks.java 2812 2015-02-17 21:00:43Z tgaskins $
 * @see gov.nasa.worldwindx.examples.PlacemarkLabelEditing
 */
public class Placemarks extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            super(true, true, true);
            
             // Create a screen selector to display a screen selection rectangle and track the objects intersecting
        // that rectangle.
            ScreenSelector screenSelector = new ScreenSelector(this.getWwd());

        // Set up a custom highlight controller that highlights objects both under the cursor and inside the
        // selection rectangle. Disable the superclass' default highlight controller to prevent it from interfering
        // with our highlight controller.
            ScreenSelection.SelectionHighlightController selectionHighlightController = new ScreenSelection.SelectionHighlightController(this.getWwd(), screenSelector);
            
            screenSelector.enable();

            //this.getWwd().getSceneController().setClutterFilter(new PlacemarkClutterFilter());
            final RenderableLayer layer = new RenderableLayer();

            MGRSGraticuleLayer mgrsLayer = new MGRSGraticuleLayer();
            getWwd().getModel().getLayers().add(mgrsLayer);

            getWwd().getView().getViewInputHandler().setEnableSmoothing(false);

           // getWwd().getView().getViewPropertyLimits().setPitchLimits(Angle.ZERO, Angle.ZERO);

            PointPlacemark pp = new PointPlacemark(Position.fromDegrees(28, -102, 1e4));
            //pp.setEnableDecluttering(true);
            pp.setLabelText("Placemark A");
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Label, Semi-transparent, Audio icon");
            pp.setLineEnabled(false);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            pp.setEnableLabelPicking(true); // enable label picking for this placemark
            PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
            attrs.setImageAddress("gov/nasa/worldwindx/examples/images/audioicon-64.png");
            attrs.setImageColor(new Color(1f, 1f, 1f, 0.6f));
            attrs.setScale(0.6);
//            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            layer.addRenderable(pp);

            // Place a default pin placemark at the same location over the previous one.
            pp = new PointPlacemark(pp.getPosition());
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Default icon over audio icon");
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            layer.addRenderable(pp);

            pp = new PointPlacemark(Position.fromDegrees(28, -104, 1e4));
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Audio icon, Heading 90, Screen relative");
            pp.setLabelText("Placemark B");
            pp.setLineEnabled(false);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setHeading(90d);
            attrs.setHeadingReference(AVKey.RELATIVE_TO_SCREEN);
            attrs.setScale(0.6);
            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            layer.addRenderable(pp);

            // Place a pin placemark at the same location over the previous one.
            pp = new PointPlacemark(pp.getPosition());
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Default icon over rotated audio icon");
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            layer.addRenderable(pp);

            // Use a new attributes instance.
            // Note that a new attributes instance must be created for every unique set of attribute values, although
            // the new attributes can be initialized from an existing attributes instance.
            pp = new PointPlacemark(Position.fromDegrees(29, -104, 2e4));
            pp.setLabelText("Placemark C");
            pp.setValue(AVKey.DISPLAY_NAME, "Absolute, Label, Red pin icon, Line in random color and 2 wide");
            pp.setLineEnabled(true);
            pp.setAltitudeMode(WorldWind.ABSOLUTE);
            attrs = new PointPlacemarkAttributes();
            attrs.setScale(0.6);
            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            attrs.setLineMaterial(new Material(WWUtil.makeRandomColor(null)));
            attrs.setLineWidth(2d);
            attrs.setImageAddress("images/pushpins/plain-red.png");
            pp.setAttributes(attrs);
            layer.addRenderable(pp);

            // Create a placemark without a leader line.
            pp = new PointPlacemark(Position.fromDegrees(30, -104.5, 2e4));
            pp.setLabelText("Placemark D");
            pp.setValue(AVKey.DISPLAY_NAME, "Relative to ground, Label, Teal pin icon, No line");
            pp.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setImageAddress("images/pushpins/plain-teal.png");
            pp.setAttributes(attrs);
            layer.addRenderable(pp);

            // Create a placemark clamped to ground.
            pp = new PointPlacemark(Position.fromDegrees(28, -104.5, 2e4));
            pp.setLabelText("Placemark E");
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Blue label, White pin icon");
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setLabelColor("ffff0000");
            attrs.setImageAddress("images/pushpins/plain-white.png");
            pp.setAttributes(attrs);
            layer.addRenderable(pp);

            // Create a placemark that uses all default values.
            pp = new PointPlacemark(Position.fromDegrees(30, -103.5, 2e3));
            pp.setLabelText("Placemark F");
            pp.setValue(AVKey.DISPLAY_NAME, "All defaults");
            layer.addRenderable(pp);

            // Create a placemark without an image.
            pp = new PointPlacemark(Position.fromDegrees(29, -104.5, 2e4));
            pp.setLabelText("Placemark G");
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, White label, Red point, Scale 5");
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            attrs = new PointPlacemarkAttributes();
            attrs.setLabelColor("ffffffff");
            attrs.setLineColor("ff0000ff");
            attrs.setUsePointAsDefaultImage(true);
            attrs.setScale(5d);
            pp.setAttributes(attrs);
            layer.addRenderable(pp);

            // Create a placemark off the surface and with a line.
            pp = new PointPlacemark(Position.fromDegrees(30, -104, 2e4));
            pp.setLabelText("Placemark H");
            pp.setValue(AVKey.DISPLAY_NAME, "Relative to ground, Blue label, Magenta point and line, Scale 10");
            pp.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pp.setLineEnabled(true);
            attrs = new PointPlacemarkAttributes();
            attrs.setLabelColor("ffff0000");
            attrs.setLineMaterial(Material.MAGENTA);
            attrs.setLineWidth(2d);
            attrs.setUsePointAsDefaultImage(true);
            attrs.setScale(10d);
            pp.setAttributes(attrs);
            layer.addRenderable(pp);

            pp = new PointPlacemark(Position.fromDegrees(28, -103, 1e4));
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Audio icon, Heading -45, Globe relative");
            pp.setLabelText("Placemark I");
            pp.setLineEnabled(false);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setImageAddress("gov/nasa/worldwindx/examples/images/audioicon-64.png");
            attrs.setHeading(-45d);
            attrs.setHeadingReference(AVKey.RELATIVE_TO_GLOBE);
            attrs.setScale(0.6);
//            attrs.setImageOffset(new Offset(0.5, 0.5, AVKey.FRACTION, AVKey.FRACTION));
            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
            attrs.setLabelColor("ffffffff");
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            layer.addRenderable(pp);

            pp = new PointPlacemark(Position.fromDegrees(30, 179.9, 100e3));
            pp.setValue(AVKey.DISPLAY_NAME, "Near dateline,  Clamp to ground, NASA icon, Heading -45, Globe relative");
            pp.setLabelText("Placemark J");
            pp.setLineEnabled(false);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setImageAddress("gov/nasa/worldwindx/examples/images/georss.png");
            attrs.setHeading(-45d);
            attrs.setHeadingReference(AVKey.RELATIVE_TO_GLOBE);
            attrs.setScale(0.6);
            attrs.setLabelColor("ffffffff");
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            layer.addRenderable(pp);

            pp = new PointPlacemark(Position.fromDegrees(90, 0, 100e3));
            pp.setValue(AVKey.DISPLAY_NAME, "North Pole,  Clamp to ground, NASA icon, Heading -45, Globe relative");
            pp.setLabelText("Placemark K");
            pp.setLineEnabled(false);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setImageAddress("gov/nasa/worldwindx/examples/images/georss.png");
            attrs.setHeading(-45d);
            attrs.setHeadingReference(AVKey.RELATIVE_TO_GLOBE);
            attrs.setScale(0.6);
            attrs.setLabelColor("ffffffff");
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            layer.addRenderable(pp);

            // Create a placemark that uses a 2525C tactical symbol. The symbol is downloaded from the internet on a
            // separate thread.
            WorldWind.getTaskService().addTask(new Runnable() {
                @Override
                public void run() {
                    createTacticalSymbolPointPlacemark(layer);
                }
            });

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void createTacticalSymbolPointPlacemark(final RenderableLayer layer) {
        // *** This method is running on thread separate from the EDT. ***

        // Create an icon retriever using the path specified in the config file, or the default path.
//        String iconRetrieverPath = Configuration.getStringValue(AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH,
//                MilStd2525Constants.DEFAULT_ICON_RETRIEVER_PATH);
        List<String> imageList = new ArrayList<>();
        IconRetriever iconRetriever = new MilStd2525IconRetriever("jar:file:testData/milstd2525-symbols.zip!");
        String iconRetrieverPath = Configuration.getStringValue(AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH,
                "jar:file:testData/milstd2525-symbols.zip!");
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile("testData/milstd2525-symbols.zip");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String s = entry.getName();
            if (s.contains("war") && s.contains(".png")) {
                String milStdIconName = s.split("/")[2].split("\\.")[0];
                //AVList params = new AVListImpl();
                //BufferedImage symbolImage = iconRetriever.createIcon(milStdIconName, params);
                imageList.add(milStdIconName);

            }
        }

        Random random = new Random();
        final BasicTacticalSymbolAttributes sharedHighlightAttrs = new BasicTacticalSymbolAttributes();
        sharedHighlightAttrs.setInteriorMaterial(Material.WHITE);
        sharedHighlightAttrs.setTextModifierMaterial(Material.WHITE);
        sharedHighlightAttrs.setOpacity(1.0);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        //AVList params = new AVListImpl();
        // Create an alternate version of the image that we'll use for highlighting.
        //params.setValue(AVKey.COLOR, Color.WHITE);
        //final BufferedImage highlightImage = iconRetriever.createIcon("SFAPMFQM--GIUSA", params);
        TacticalSymbol.LODSelector lodSelector = new TacticalSymbol.LODSelector() {
            @Override
            public void selectLOD(DrawContext dc, TacticalSymbol symbol, double eyeDistance) {
                // Show text modifiers only when eye distance is less than 50 km.
                if (eyeDistance < 50e3) {
                    symbol.setShowTextModifiers(true);
                } else {
                    symbol.setShowTextModifiers(false);
                }

                // Scale the symbol when the eye distance is between 1 and 100 km. The symbol is scaled between
                // 100% and 25% of its normal size.
                // The scale is an attribute, so determine which attributes -- normal or highlight -- need to be
                // set for this rendering.
                TacticalSymbolAttributes attributes = symbol.isHighlighted()
                        ? symbol.getHighlightAttributes() : symbol.getAttributes();

                double minScaleDistance = 1e3;
                double maxScaleDistance = 100e3;

                if (eyeDistance > minScaleDistance && eyeDistance < maxScaleDistance) {
                    double scale = 0.5;
                    //+ (maxScaleDistance - eyeDistance) / (maxScaleDistance - minScaleDistance);
                    attributes.setScale(scale);
                    // System.out.println(scale);
                } else {
                    attributes.setScale(.5);
                }

                // Show only the symbol's alternate representation when the eye distance is greater than 100 km.
                if (dc.getView().getEyePosition().getAltitude() < 1000e3) {
                    symbol.setShowGraphicModifiers(true);
                    ((MilStd2525TacticalSymbol) symbol).setShowFrame(true);
                    ((MilStd2525TacticalSymbol) symbol).setShowIcon(true);
                } else if (dc.getView().getEyePosition().getAltitude() < 10000e3) {
                    symbol.setShowGraphicModifiers(false);
                    ((MilStd2525TacticalSymbol) symbol).setShowFrame(true);
                    ((MilStd2525TacticalSymbol) symbol).setShowIcon(false);
                } else {
                    // Setting the symbol's show-frame and  show-icon properties to false causes the symbol's
                    // alternate representation to be drawn. The alternate representation is a filled circle.
                    symbol.setShowGraphicModifiers(false);
                    ((MilStd2525TacticalSymbol) symbol).setShowFrame(false);
                    ((MilStd2525TacticalSymbol) symbol).setShowIcon(false);
                }
            }
        };

        // Add the placemark to WorldWind on the event dispatch thread.
        SwingUtilities.invokeLater(new Runnable() {
            double minLat = 35, maxLat = 45, minLon = -85, maxLon = -80;
            double delta = 0.04;

            @Override
            public void run() {
                try {

                    int count = 0;
                    for (double lat = minLat; lat <= maxLat; lat += delta) {
                        for (double lon = minLon; lon <= maxLon; lon += delta) {

                            TacticalSymbolAttributes attributes = new BasicTacticalSymbolAttributes();
                            attributes.setTextModifierMaterial(Material.RED);

                            attributes.setScale(0.1);
                            MilStd2525TacticalSymbol airSymbol = new MilStd2525TacticalSymbol("SFGCUCDMLA-GCAG",
                                    Position.fromDegrees(lat, lon, 3000));

                            airSymbol.setEnableBatchRendering(true);
                            airSymbol.setDragEnabled(true);
                            airSymbol.setValue(AVKey.DISPLAY_NAME, imageList.get(random.nextInt(6800))); // Tool tip text.
                            airSymbol.setAttributes(attributes);
                             airSymbol.setHighlightAttributes(sharedHighlightAttrs);
                            airSymbol.setModifier(SymbologyConstants.DIRECTION_OF_MOVEMENT, Angle.fromDegrees(335));
                            airSymbol.setModifier(SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE, true);
                            airSymbol.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                           
                            airSymbol.setShowLocation(true);
                            airSymbol.setLODSelector(lodSelector); // specify the LOD selector
                            layer.addRenderable(airSymbol);

                            // Create a ground tactical symbol for the MIL-STD-2525 symbology set.
                            // Create a ground tactical symbol for the MIL-STD-2525 symbology set.
                            //WayPointDiamond w = new WayPointDiamond();
                            // w.setPosition(Position.fromDegrees(lat, lon, 0));
                            // layer.addRenderable(w);
//                            PointPlacemark pp = new PointPlacemark(Position.fromDegrees(lat, lon, 0));
//                           // pp.setLabelDrawElevation(500000.0d);
//
//                            pp.setEnableBatchRendering(true);
//                            // pp.setEnableDecluttering(true); // enable the placemark for decluttering
//                            //pp.setEnableLabelPicking(true); // enable the placemark for label picking
//                            pp.setLabelText("Entity " + (count + 1));
//                            pp.setLineEnabled(false);
//                            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
//                            pp.setEnableLabelPicking(true);
//                            pp.setAlwaysOnTop(true); // Set this flag just to show how to force the placemark to the top
//
//                            // Create and assign the placemark attributes.
//                            PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
//                            attrs.setHeading(0.0);
//                            attrs.setHeadingReference(AVKey.RELATIVE_TO_GLOBE);
//                            BufferedImage image = imageList.get(random.nextInt(6000));
//                            attrs.setImage(image);
//                            attrs.setImageColor(new Color(1f, 1f, 1f, 1f));
//                            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
//                            attrs.setScale(0.2);
//
//                            pp.setAttributes(attrs);
//
//                            // Create and assign the placemark's highlight attributes.
//                            PointPlacemarkAttributes highlightAttributes = new PointPlacemarkAttributes(attrs);
//                            highlightAttributes.setImage(highlightImage);
//                            pp.setHighlightAttributes(highlightAttributes);
                            // Add the placemark to the layer.
                            // layer.addRenderable(pp);
                            ++count;
                        }
                    }

                    System.out.println("count: " + count);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        
        Configuration.setValue("gov.nasa.worldwind.avkey.MilStd2525IconRetrieverPath", "jar:file:testData/milstd2525-symbols.zip!");

        ApplicationTemplate.start("WorldWind Placemarks", AppFrame.class);
    }
}
