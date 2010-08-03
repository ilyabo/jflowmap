/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.views;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import jflowmap.FlowMapColorSchemes;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.AreaMap;
import jflowmap.views.flowmap.ColorSchemeAware;
import jflowmap.views.flowmap.VisualAreaMap;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.PFrame;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class VisualAreaMapInCameraTest extends PFrame {

  private final ColorSchemeAware mapColorScheme = new ColorSchemeAware() {
    @Override
    public Color getColor(ColorCodes code) {
      return FlowMapColorSchemes.INVERTED.get(code);
    }
  };
  private final VisualAreaMap map;
  private final PPath redRect;
  private final PPath blueRect;

  public VisualAreaMapInCameraTest() throws IOException {
    setSize(800, 600);
//    setFullScreenMode(true);
    PCanvas canvas = getCanvas();
    PCamera camera = canvas.getCamera();

    map = new VisualAreaMap(mapColorScheme,
        AreaMap.load("data/refugees/countries-areas-ll.xml.gz"),
        MapProjections.MERCATOR);

//    PLayer layer = canvas.getLayer();
//    layer.addChild(map);
    camera.addChild(map);

//    map.setScale(100);

    PBounds mapFB = map.getFullBounds();
    redRect = new PPath(new Rectangle2D.Double(mapFB.x, mapFB.y, mapFB.width, mapFB.height));
    redRect.setStrokePaint(Color.red);
    redRect.setStroke(new PFixedWidthStroke(1.0f));
    camera.addChild(redRect);

    blueRect = new PPath(new Rectangle2D.Double(0,0,1,1));
    blueRect.setStrokePaint(Color.blue);
    blueRect.setStroke(new PFixedWidthStroke(1.0f));
    camera.addChild(blueRect);

    camera.addPropertyChangeListener(new CameraListener());

    fitInView();

  }

  private class CameraListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
        final String prop = evt.getPropertyName();
        if (prop == PCamera.PROPERTY_VIEW_TRANSFORM) {
//          updateMapToMatrixLines();
        } else if (prop == PCamera.PROPERTY_BOUNDS) {
//          anchorRightVisualAreaMap();
//          updateMapToMatrixLines();
          fitInView();
        }
    }

  }


  private void fitInView() {
    PCamera camera = getCanvas().getCamera();
    PBounds cameraBounds = camera.getBounds();


//    PBounds mapBounds = map.getBounds();
//    PBounds mapFullBounds = map.getFullBounds();

//    Rectangle2D.Double actualMapBounds = getActualMapBounds(mapFullBounds);
//
//
//    double width = cameraBounds.getWidth() / 1;
//    double height = cameraBounds.getHeight();
//    double scale = Math.min(width / actualMapBounds.width, height / actualMapBounds.height);
//
////    map.setScale(scale);
//
//    actualMapBounds = getActualMapBounds(mapFullBounds);
//
//    map.setOffset(
////        cameraBounds.getMinX() - actualMapBounds.getX(),
//     cameraBounds.getMaxX() - actualMapBounds.getMaxX(),
//        //cameraBounds.getMinY() - actualMapBounds.getMinY() +
//        (cameraBounds.getMaxY() + actualMapBounds.getMaxY()) / 2
//    );







//    map.setScale(.5);

    PBounds mapFullBounds = map.getUnionOfChildrenBounds(null);

    double scale = Math.min(camera.getWidth() / mapFullBounds.width / 3, camera.getHeight() / mapFullBounds.height);
    if (scale <= 0) {
      scale = 1.0;
    }
    map.setScale(scale);
    map.setOffset(
       cameraBounds.getMaxX() - mapFullBounds.getMaxX() * scale,
        (cameraBounds.getMaxY() + mapFullBounds.getMaxY() * scale) / 2
    );




    blueRect.setBounds(camera.getBounds().getBounds2D());
    redRect.setBounds(map.getFullBounds().getBounds2D());

    repaint();
  }



  private Rectangle2D.Double getActualMapBounds(PBounds mapFullBounds) {
    Rectangle2D.Double actualMapBounds = new Rectangle2D.Double();
    map.getTransform().inverseTransform(mapFullBounds.getBounds2D(), actualMapBounds);
    return actualMapBounds;
  }



  public static void main(String[] args) {

    try {
      new VisualAreaMapInCameraTest().setVisible(true);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
