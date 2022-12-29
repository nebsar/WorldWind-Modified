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

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.BasicTacticalSymbolAttributes;
import gov.nasa.worldwind.symbology.IconRetriever;
import gov.nasa.worldwind.symbology.SymbologyConstants;
import gov.nasa.worldwind.symbology.TacticalSymbol;
import gov.nasa.worldwind.symbology.TacticalSymbolAttributes;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525IconRetriever;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525TacticalSymbol;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import gov.nasa.worldwindx.examples.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Demonstrates how to use the {@link gov.nasa.worldwindx.examples.util.ScreenSelector} utility to perform
 * multiple-object selection in screen space.
 *
 * @author dcollins
 * @version $Id: ScreenSelection.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ScreenSelection extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected ScreenSelector screenSelector;
        protected SelectionHighlightController selectionHighlightController;

        public AppFrame()
        {
            // Create a screen selector to display a screen selection rectangle and track the objects intersecting
            // that rectangle.
            this.screenSelector = new ScreenSelector(this.getWwd());

            // Set up a custom highlight controller that highlights objects both under the cursor and inside the
            // selection rectangle. Disable the superclass' default highlight controller to prevent it from interfering
            // with our highlight controller.
            this.selectionHighlightController = new SelectionHighlightController(this.getWwd(), this.screenSelector);
            this.getWwjPanel().highlightController.dispose();
            
            this.getWwd().getSceneController().setDeepPickEnabled(true);

            // Create a button to enable and disable screen selection.
            JButton btn = new JButton(new EnableSelectorAction());
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.add(btn, BorderLayout.CENTER);
            this.getControlPanel().add(panel, BorderLayout.SOUTH);

            // Create layer of highlightable shapes to select.
            this.addShapes();
        }

        protected void addShapes()
        {
            RenderableLayer layer = new RenderableLayer();
            
            createTacticalSymbolPointPlacemark(layer);

            

            this.getWwd().getModel().getLayers().add(layer);
        }

        protected class EnableSelectorAction extends AbstractAction
        {
            public EnableSelectorAction()
            {
                super("Start");
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                ((JButton) actionEvent.getSource()).setAction(new DisableSelectorAction());
                screenSelector.enable();
            }
        }

        protected class DisableSelectorAction extends AbstractAction
        {
            public DisableSelectorAction()
            {
                super("Stop");
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                ((JButton) actionEvent.getSource()).setAction(new EnableSelectorAction());
                screenSelector.disable();
            }
        }
    }

    /**
     * Extends HighlightController to add the capability to highlight objects selected by a ScreenSelector. This tracks
     * objects highlighted by both cursor rollover events and screen selection changes, and ensures that objects stay
     * highlighted when they are either under cursor or in the ScreenSelector's selection rectangle.
     */
    protected static class SelectionHighlightController extends HighlightController implements MessageListener
    {
        protected ScreenSelector screenSelector;
        protected PickedObjectList lastBoxHighlightObjects = new PickedObjectList();

        public SelectionHighlightController(WorldWindow wwd, ScreenSelector screenSelector)
        {
            super(wwd, SelectEvent.ROLLOVER);

            this.screenSelector = screenSelector;
            this.screenSelector.addMessageListener(this);
        }

        @Override
        public void dispose()
        {
            super.dispose();

            this.screenSelector.removeMessageListener(this);
        }

        public void onMessage(Message msg)
        {
            try
            {
                // Update the list of highlighted objects whenever the ScreenSelector's selection changes. We capture
                // both the selection started and selection changed events to ensure that we clear the list of selected
                // objects when the selection begins or re-starts, as well as update the list when it changes.
                if (msg.getName().equals(ScreenSelector.SELECTION_STARTED)
                    || msg.getName().equals(ScreenSelector.SELECTION_ENDED))
                {
                    this.highlightSelectedObjects(this.screenSelector.getSelectedObjects());
                }
            }
            catch (Exception e)
            {
                // Wrap the handler in a try/catch to keep exceptions from bubbling up
                Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
            }
        }

        protected void highlight(Object o)
        {
            // Determine if the highlighted object under the cursor has changed, but should remain highlighted because
            // its in the selection box. In this case we assign the highlighted object under the cursor to null and
            // return, and thereby avoid changing the highlight state of objects still highlighted by the selection box.
            List allObjects = new ArrayList();
            Iterator iter = lastBoxHighlightObjects.iterator();
            while (iter.hasNext()) {
                PickedObject pickedObject = (PickedObject) iter.next();
                allObjects.add(pickedObject.getObject());
            }

            if (this.lastHighlightObject != o && allObjects.contains(this.lastHighlightObject)) {
                this.lastHighlightObject = null;
                return;
            }

            super.highlight(o);
        }

        protected void highlightSelectedObjects(List<?> list)
        {
            if (this.lastBoxHighlightObjects.equals(list))
                return; // same thing selected

            // Turn off highlight for the last set of selected objects, if any. Since one of these objects may still be
            // highlighted due to a cursor rollover, we detect that object and avoid changing its highlight state.
            // Turn off highlight for the last set of selected objects, if any. Since one of these objects may still be
            // highlighted due to a cursor rollover, we detect that object and avoid changing its highlight state.
            for (PickedObject h : this.lastBoxHighlightObjects) {

                if (h.getObject() != this.lastHighlightObject)
                    ((Highlightable) h.getObject()).setHighlighted(false);
            }
            this.lastBoxHighlightObjects.clear();

            if (list != null) {
                // Turn on highlight if object selected.
                for (Object o : list) {
                    if (o instanceof PickedObject) {
                        PickedObject po = (PickedObject) o;
                        if (po.getObject() instanceof Highlightable) {
                            Highlightable highlightable = (Highlightable) po.getObject();
                            highlightable.setHighlighted(true);
                            this.lastBoxHighlightObjects.add(po);
                        }

                    }
                }
            }

            // We've potentially changed the highlight state of one or more objects. Request that the WorldWindow
            // redraw itself in order to refresh these object's display. This is necessary because changes in the
            // objects in the pick rectangle do not necessarily correspond to mouse movements. For example, the pick
            // rectangle may be cleared when the user releases the mouse button at the end of a drag. In this case,
            // there's no mouse movement to cause an automatic redraw.
            this.wwd.redraw();
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
                            //airSymbol.setEnableBatchPicking(false);
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

    public static void main(String[] args)
    {
        start("WorldWind Screen Selection", AppFrame.class);
    }
}
