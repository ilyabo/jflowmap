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

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jflowmap.JFlowMapApplet;
import jflowmap.util.SwingUtils;
import jflowmap.util.piccolo.PBoxLayoutNode;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PanHandler;
import jflowmap.util.piccolo.PiccoloUtils;
import jflowmap.util.piccolo.ZoomHandler;

import org.apache.batik.ext.awt.image.codec.png.PNGImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.log4j.Logger;

import at.fhj.utils.swing.JMsgPane;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class VisualCanvas extends PCanvas {

  private static final String ACTION_SVG_EXPORT = "svg-export";

  private static Logger logger = Logger.getLogger(VisualCanvas.class);

  private static final Dimension MIN_SIZE = new Dimension(150, 100);
  private ZoomHandler zoomHandler;
  private boolean autoFitOnBoundsChange = true;
  private final PBoxLayoutNode settingButtonsPanel;
//  private BlockingGlassPane blockingGlassPane;

  public VisualCanvas() {
    setZoomEventHandler(null);
    setZoomHandler(createZoomHandler());
    setPanEventHandler(new PanHandler());
    getInputMap().put(KeyStroke.getKeyStroke("alt S"), ACTION_SVG_EXPORT);
    getActionMap().put(ACTION_SVG_EXPORT, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        tryToPaintToSvg();
      }
    });

    settingButtonsPanel = new PBoxLayoutNode(PBoxLayoutNode.Axis.X, 5);
    final PCamera ccam = getCamera();
    ccam.addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        PBounds b = ccam.getBoundsReference();
        PNodes.setPosition(settingButtonsPanel, b.getMaxX() - settingButtonsPanel.getFullBoundsReference().width - 4, 4);
      }
    });
    ccam.addChild(settingButtonsPanel);

  }

  public PBoxLayoutNode getSettingButtonsPanel() {
    return settingButtonsPanel;
  }

  public ZoomHandler getZoomHandler() {
    return zoomHandler;
  }

  public void setZoomHandler(ZoomHandler handler) {
    if (zoomHandler != null) {
      removeInputEventListener(zoomHandler);
    }
    zoomHandler = handler;
    addInputEventListener(zoomHandler);
  }

  protected ZoomHandler createZoomHandler() {
    return new ZoomHandler();
  }

  public boolean getAutoFitOnBoundsChange() {
    return autoFitOnBoundsChange;
  }

  public void setAutoFitOnBoundsChange(boolean autoFitOnBoundsChange) {
    this.autoFitOnBoundsChange = autoFitOnBoundsChange;
  }

  public void setViewZoomPoint(Point2D point) {
    zoomHandler.setFixedViewZoomPoint(point);
  }

  public void setMinZoomScale(double minScale) {
    zoomHandler.setMinScale(minScale);
  }

  public void setMaxZoomScale(double maxScale) {
    zoomHandler.setMaxScale(maxScale);
  }

  public void fitChildrenInCameraView() {
    PCamera camera = getCamera();
    PBounds boundRect = getLayer().getFullBounds();
    camera.globalToLocal(boundRect);
    camera.animateViewToCenterBounds(boundRect, true, 0);
  }

  @Override
  public Dimension getMinimumSize() {
    return MIN_SIZE;
  }

  @Override
  public void setBounds(int x, int y, int w, int h) {
    if (autoFitOnBoundsChange) {
      if (x != getX()  ||  y != getY()  ||  w != getWidth()  ||  h != getHeight()) {
        Rectangle2D oldVisibleBounds =  getVisibleBounds();
        Rectangle2D oldContentBounds = getViewContentBounds();

        super.setBounds(x, y, w, h);

        if (oldVisibleBounds != null) {
          fitVisibleInCameraView(oldVisibleBounds, oldContentBounds);
        }
      }
    } else {
      super.setBounds(x, y, w, h);
    }
  }

  private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);

  public void fitVisibleInCameraView(Rectangle2D visibleBounds, Rectangle2D contentBounds) {
    if (contentBounds.getWidth() <= 0 || contentBounds.getHeight() <= 0) {
      return;
    }
    final Insets insets;
//    if (contentBounds.contains(visibleBounds)) { // the whole  is inside the view
//      insets = getContentInsets();
//      insets.left += 5;
//      insets.top += 5;
//      insets.bottom += 5;
//      insets.right += 5;
//    } else {
      insets = NULL_INSETS;
//    }
    PiccoloUtils.setViewPaddedBounds(getCamera(), visibleBounds, insets);
  }


  /**
   * Bounds of the part of the visualization (all of the Nodes together) that
   * is currently visible in the view (in the view coordinate system).
   *
   * @return
   */
  public Rectangle2D getVisibleBounds() {
//      final PBounds mb = getBounds();
      final PBounds mb = getLayer().getFullBounds();
      if (mb != null) {
          final PBounds vb = getCamera().getViewBounds();
          Rectangle2D.intersect(vb, mb, vb);
          return vb;
      }
      return null;
  }

  /**
   * Bounds of the area where the visualization should be placed (without the floating
   * panels) in the camera coordinate system.
   *
   * @return
   */
  private Rectangle2D getContentBounds() {
//    final Insets insets = getContentInsets();
//    final PBounds vb = getCamera().getBounds();
//    vb.x += insets.left;
//    vb.y += insets.top;
//    vb.width -= insets.left + insets.right;
//    vb.height -= insets.top + insets.bottom;
//    return vb;
    return getBounds();
  }

  private Rectangle2D getViewContentBounds() {
    final Rectangle2D cb = getContentBounds();
    getCamera().localToView(cb);
    return cb;
  }

  public void tryToPaintToSvg() {
    try {
      try {
        File f = getOutputFilename(".svg");
        paintToSvg(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        JMsgPane.showInfoDialog(VisualCanvas.this, "View exported to '" + f.getAbsolutePath() + "'");
      } catch (SecurityException se) {
        // try again, but show the output in window
        StringWriter sw = new StringWriter();
        paintToSvg(sw);
        showSvgInApplet(sw.getBuffer().toString());
      }
    } catch (Exception ex) {
      logger.error("Cannot export SVG", ex);
      showProblemDialog(ex);
    }
  }

  public void tryToPaintToPng() {
    try {
      File f = getOutputFilename(".png");
      paintToPng(new FileOutputStream(f));
      JMsgPane.showInfoDialog(VisualCanvas.this, "View exported to '" + f.getAbsolutePath() + "'");
    } catch (Exception ex) {
      logger.error("Cannot export PNG", ex);
      showProblemDialog(ex);
    }
  }

  public void showProblemDialog(Exception ex) {
    JMsgPane.showProblemDialog(this,
        ex.getClass().getSimpleName() + " " + ex.getMessage() + " " +
        (ex.getCause() != null ? ex.getCause().getMessage() : ""));
  }

  public void showSvgInApplet(String svgCode) throws Exception {
    // ShowSourceDialog dialog = new ShowSourceDialog(
    // SwingUtils.getWindowFor(this), "SVG export", svgCode, false);
    // dialog.setVisible(true);

    Applet applet = SwingUtils.getAppletFor(this);
    if (applet != null  &&  applet instanceof JFlowMapApplet) {
      ((JFlowMapApplet)applet).jsFlowMapFunctionCall("showSVGCode",  new Object[] { svgCode });
    }
  }

  public void paintToSvg(final Writer out) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();

    SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(builder.newDocument());
    ctx.setExtensionHandler(new SvgGenExtensionHandler());

    SVGGraphics2D svgGen = new SVGGraphics2D(ctx, false);

    ImageWriterRegistry.getInstance().register(new PNGImageWriter());
    paintComponent(svgGen);

    try {
      svgGen.stream(out, false);
      out.flush();
    } finally {
      if (out != null) out.close();
    }
  }

  public void paintToPng(OutputStream out) throws IOException {
    BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

    Graphics2D g = (Graphics2D)image.getGraphics();
    paintComponent(g);

    try {
      ImageIO.write(image, "PNG", out);
    } finally {
      if (out != null) out.close();
    }
  }


//  public void setBlockInput(boolean block) {
//    if (blockingGlassPane == null) {
//      initGlassPane();
//    }
//    blockingGlassPane.setVisible(block);
//  }
//
//  private void initGlassPane() {
//    blockingGlassPane = new BlockingGlassPane();
//    blockingGlassPane.setVisible(false);
//
//    JInternalFrame iframe = SwingUtils.getInternalFrameFor(this);
//    if (iframe != null) {
//      iframe.setGlassPane(blockingGlassPane);
//    } else {
//      Window w = SwingUtils.getWindowFor(this);
//      if (w != null  &&  (w instanceof JFrame)) {
//        ((JFrame)w).setGlassPane(blockingGlassPane);
//      } else {
//        JApplet applet = SwingUtils.getAppletFor(this);
//        applet.setGlassPane(blockingGlassPane);
//      }
//    }
//  }

  public File getOutputFilename(String ext) {
    File file = new File("output" + ext);
    int i = 0;
    while (file.exists()) {
      i++;
      file = new File("output-" + i + ext);
    }
    return file;
  }
}


