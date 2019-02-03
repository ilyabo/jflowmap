/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.views.flowstrates;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.util.Pair;
import jflowmap.util.piccolo.PLabel;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PTypedBasicInputEventHandler;
import jflowmap.util.piccolo.PiccoloUtils;
import prefuse.data.Edge;
import at.fhjoanneum.cgvis.data.IColorForValue;
import at.fhjoanneum.cgvis.data.IDataValues;
import at.fhjoanneum.cgvis.plots.AbstractFloatingLabelsNode.LabelIterator;
import at.fhjoanneum.cgvis.plots.AbstractFloatingLabelsNode.LabelPositioner;
import at.fhjoanneum.cgvis.plots.PaintedFloatingLabelsNode;
import at.fhjoanneum.cgvis.plots.mosaic.MosaicPlotNode;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Ilya Boyandin
 */
public class FastHeatmapLayer extends AbstractHeatmapLayer {

  private static final Color LABEL_BACKGROUND = new Color(255, 255, 255, 0);
  private static final Font NODE_LABELS_FONT = new Font("Arial", Font.PLAIN, 9);
  private static final Font ATTR_LABELS_FONT = NODE_LABELS_FONT;
  private static final Color FLOATING_LABELS_BG = new Color(255, 255, 255, 255);
  private MosaicPlotNode heatmapNode;
  private final IColorForValue colorForValue;
  private final InteractiveFloatingLabelsNode attrLabelsNode;
  private final PaintedFloatingLabelsNode originLabelsNode;
  private final PaintedFloatingLabelsNode destLabelsNode;
  private Pair<List<String>,List<String>> nodeLabels;
  private final FontMetrics nodeLabelsFontMetrics;
  private final FastHeatmapCursor cursor;

  public FastHeatmapLayer(FlowstratesView flowstratesView) {
    super(flowstratesView);
    colorForValue = createColorForValue();
    nodeLabelsFontMetrics = getFlowstratesView().getVisualCanvas().getFontMetrics(NODE_LABELS_FONT);

    renew();

    getCamera().setComponent(getFlowstratesView().getVisualCanvas());

    originLabelsNode = createFloatingLabels(createNodeLabelIterator(FlowEndpoint.ORIGIN), true);
    destLabelsNode = createFloatingLabels(createNodeLabelIterator(FlowEndpoint.DEST), false);
    attrLabelsNode = createAttrFloatingLabels(); // true, createAttrsLabelIterator(), false);

//    originLabelsNode.addDisjointNode(attrLabelsNode);
//    destLabelsNode.addDisjointNode(attrLabelsNode);

    getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        adjustFloatingLabelNodePositions();
        attrLabelsNode.positionLabels();
      }
    });

    cursor = new FastHeatmapCursor(this);
    getCamera().addChild(cursor);
    cursor.moveToBack();

    originLabelsNode.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        FlowstratesView fs = getFlowstratesView();
        /*if (fs.getFlowLinesLayerNode().getShowAllFlowLines())*/ {
          fs.setRowOrdering(RowOrderings.SRC_VPOS);
        }
      }
    });
    destLabelsNode.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        FlowstratesView fs = getFlowstratesView();
        /*if (fs.getFlowLinesLayerNode().getShowAllFlowLines())*/ {
          fs.setRowOrdering(RowOrderings.TARGET_VPOS);
        }
      }
    });
  }

  public MosaicPlotNode getHeatmapNode() {
    return heatmapNode;
  }

  private PaintedFloatingLabelsNode createFloatingLabels(LabelIterator<String> it, boolean anchorLabelsToEnd) {
    PaintedFloatingLabelsNode labels = new PaintedFloatingLabelsNode(false, it) {
      @Override
      protected void paint(PPaintContext pc) {
        // workaround to avoid one line of empty space which showes up
        // mysteriously between the floating label panels when using disjoint nodes
        Graphics2D g2 = pc.getGraphics();
        Shape oldClip = g2.getClip();
        g2.setClip(getBounds());
        super.paint(pc);
        g2.setClip(oldClip);
      }
    };
    labels.setFont(NODE_LABELS_FONT);
    labels.setAnchorLabelsToEnd(anchorLabelsToEnd);
    labels.setMarginBefore(anchorLabelsToEnd ? 0 : 3);
    labels.setMarginAfter(anchorLabelsToEnd ? 3 : 0);
    labels.setPaint(FLOATING_LABELS_BG);
//    labels.setPickable(false);
    getCamera().addChild(labels);
    return labels;
  }

  private InteractiveFloatingLabelsNode createAttrFloatingLabels() {
    LabelIterator<PLabel> itr = createPNodeAttrsLabelIterator();
    LabelPositioner<PLabel> posr = new LabelPositioner<PLabel>() {

      @Override
      public void showSpacer(int x, int y) {
        // TODO: maybe show a "..." node somewhere
      }

      @Override
      public void showLabel(PLabel label, int index, int x, int y) {
        PBounds panelb = label.getParent().getBoundsReference();
        PBounds fb = label.getFullBoundsReference();
        label.setOffset(x - fb.width/2, panelb.getMaxY() - fb.height*0.37);

        Point2D tb = label.getOffset()  ; //getFullBoundsReference();
        if (tb.getX() + fb.width*.6 < originLabelsNode.getBoundsReference().getMaxX() ||
            tb.getX() + fb.width*.1 > destLabelsNode.getBoundsReference().getX()) {
          label.setTransparency(.1f);
          label.setPickable(false);
        } else {
          label.setTransparency(1f);
          label.setPickable(true);
        }
        label.setVisible(true);
      }

      @Override
      public void hideLabel(PLabel label, int count) {
        label.setVisible(false);
      }

    };
    InteractiveFloatingLabelsNode labels = new InteractiveFloatingLabelsNode(true, itr, posr);
//    PaintedFloatingLabelsNode labels = new PaintedFloatingLabelsNode(
//        true, createStringAttrsLabelIterator());
//    labels.setRotateHorizLabels(true);
//    labels.setAnchorLabelsToEnd(false);
//    labels.setFont(NODE_LABELS_FONT);
    labels.setDrawSpacers(false);
    labels.setMarginBefore(3);
    labels.setMarginAfter(0);
    labels.setPaint(FLOATING_LABELS_BG);
    getCamera().addChild(labels);
    return labels;
  }


  @Override
  public void renew() {
    super.renew();


    removeAllChildren();
//    getCamera().removeAllChildren();

    nodeLabels = null;

    IDataValues data = getDataValues();
    FlowstratesView fs = getFlowstratesView();
    heatmapNode = new MosaicPlotNode(data, fs.getVisualCanvas(), colorForValue);

    Color missingColor = fs.getStyle().getMissingValueColor();
    heatmapNode.setMissingValueColor1(missingColor);
    heatmapNode.setMissingValueColor2(missingColor);

    addChild(heatmapNode);
  }

  private IColorForValue createColorForValue() {
    return new IColorForValue() {
      @Override
      public Color getColorForValue(double value) {
        return getFlowstratesView().getColorFor(value);
      }
    };
  }

  private IDataValues getDataValues() {
    return new IDataValues() {
      FlowstratesView fs = getFlowstratesView();
      FlowMapGraph fmg = fs.getFlowMapGraph();
      List<String> attrs = fmg.getEdgeWeightAttrs();
      List<Edge> edges = fs.getVisibleEdges();
      @Override
      public int getSize() {
        return edges.size();
      }
      @Override
      public int getDimension() {
        return fmg.getEdgeWeightAttrsCount();
      }
      @Override
      public double getValue(int element, int attribute) {
        return getFlowstratesView().getValue(edges.get(element), attrs.get(attribute));
      }
    };
  }


  @Override
  public void updateColors() {
//    renew();  // TODO: just repaint the heatmap
    heatmapNode.setDirty(true);
    heatmapNode.repaint();
  }

  private void adjustFloatingLabelNodePositions() {
    PBounds cb = getCamera().getBoundsReference();

    PBounds hb = heatmapNode.getBounds();
    getCamera().viewToLocal(hb);

    PNodes.setPosition(attrLabelsNode, cb.getX() + 1,
        cb.getY() + Math.max(0, hb.getMinY() - cb.getMinY() - attrLabelsNode.getHeight()));

    PNodes.setPosition(originLabelsNode,
        cb.getMinX() + Math.max(0, hb.getMinX() - cb.getMinX() - originLabelsNode.getWidth()),
        cb.getY());

    PNodes.setPosition(destLabelsNode,
        cb.getMaxX() - Math.max(destLabelsNode.getWidth(), cb.getMaxX() - hb.getMaxX()),
        cb.getY());
  }

  private static void addMargin(PBounds cb, Insets m) {
    cb.setRect(
        cb.x + m.left, cb.y + m.top,
        cb.width - m.left - m.right, cb.height - m.top - m.bottom);
  }

  @Override
  public PBounds getActualViewBounds() {
    PBounds b = getCamera().getBounds();
    b.y += attrLabelsNode.getHeight();
    getCamera().localToView(b);
    return b;
  }

  @Override
  public void fitInView(boolean animate, boolean whole) {
    PCamera camera = getCamera();

    cursor.clearHighlighted();

    PBounds full = heatmapNode.getFullBounds();
    PBounds partial = calcBoundsToFitInView();

    PBounds toFit;
//    if (firstTimeFitInView) {
//      toFit = partial;
//      firstTimeFitInView = false;
//
//    } else {

      PBounds current = camera.getBounds();
      addMargin(current, calcFloatingLabelInsets());
      camera.localToView(current);

//      double rd = Math.abs(MathUtils.relativeDiff(current.height, full.height));
  //    System.out.println(rd + " " + current + " " + full);
//      if (rd > .5) {
//        toFit = full;
//      } else {
//        toFit = partial;
//      }
//    }

/*
    if (!whole  &&  full.getHeight() > full.getWidth() * 10) {
      toFit = partial;
    } else {
      toFit = full;
    }
*/

    if (whole) {
      toFit = full;
    } else {
      toFit = partial;
    }

    PiccoloUtils.animateViewToPaddedBounds(camera, toFit, calcFloatingLabelInsets(),
        FlowstratesView.fitInViewDuration(animate));
  }

  private PBounds calcBoundsToFitInView() {
    PCamera camera = getCamera();

    PBounds b = heatmapNode.getFullBounds();  // to be adjusted
    Insets m = calcFloatingLabelInsets();


    //if (b.height > b.width * 10) {  // if the height of the heatmap is much larger than width,
                                    // show only a part of the heatmap
      PBounds cb = camera.getBounds();
      /*
      cb.setRect(                 // add margins to have the proper aspect ratio
          cb.x + m.left, cb.y + m.top,
          cb.width - m.left - m.right, cb.height - m.top - m.bottom);
          */
      addMargin(cb, m);
      camera.localToView(cb);
      b.height = b.width * (cb.height / cb.width) * 1.0;
    //}
    return b;
  }

  /** Margins to ensure there is enough space for the floating labels */
  private Insets calcFloatingLabelInsets() {
    return new Insets((int)attrLabelsNode.getHeight(), (int)originLabelsNode.getWidth(),
        0, (int)destLabelsNode.getWidth());
  }

  @Override
  public Dimension2D getEdgeLabelBounds(Edge edge, FlowEndpoint ep) {
//    int index = getFlowstratesView().getVisibleEdgeIndex(edge);
    String label = FlowstratesView.shortenNodeLabel(getFlowMapGraph().getNodeLabel(ep.nodeOf(edge)));
    PDimension d = new PDimension(
        SwingUtilities.computeStringWidth(nodeLabelsFontMetrics, label) +
        (ep == FlowEndpoint.ORIGIN ? 7 : 2),
        nodeLabelsFontMetrics.getAscent());
    getCamera().localToView(d);
    return d;
  }

  @Override
  public Point2D getFlowLineInPoint(int row, FlowEndpoint ep) {
    switch (ep) {

    case ORIGIN:
      PBounds ob = originLabelsNode.getBounds();
      ob.x -= 3;
      getCamera().localToView(ob);
      return new Point2D.Double(ob.getMaxX(), calcNodeLabelYPos(row) + heatmapNode.getCellHeight()/2.0);

    case DEST:
      PBounds db = destLabelsNode.getBounds();
      db.x += 3;
      getCamera().localToView(db);
      return new Point2D.Double(db.getMinX(), calcNodeLabelYPos(row) + heatmapNode.getCellHeight()/2.0);

    default:
      throw new AssertionError();
    }
  }

  double getTupleY(int row) {
    return row * heatmapNode.getCellHeight();
  }

  private List<String> getNodeLabels(FlowEndpoint ep) {
    if (nodeLabels == null) {
      nodeLabels = Pair.of(nodeLabels(FlowEndpoint.ORIGIN), nodeLabels(FlowEndpoint.DEST));
    }

    switch (ep) {
    case ORIGIN: return nodeLabels.first();
    case DEST: return nodeLabels.second();
    default: throw new AssertionError();
    }
  }

  private List<String> nodeLabels(final FlowEndpoint ep) {
    List<Edge> edges = getFlowstratesView().getVisibleEdges();
    final FlowMapGraph fmg = getFlowstratesView().getFlowMapGraph();

    return Lists.transform(edges, new Function<Edge, String>() {
      @Override public String apply(Edge e) { return fmg.getNodeLabel(ep.nodeOf(e)); }
    });
  }

  private double calcNodeLabelYPos(int index) {
    return
      heatmapNode.getBoundsReference().getY() +
      index * (heatmapNode.getCellHeight() + heatmapNode.getCellSpacing());
  }

  private LabelIterator<PLabel> createPNodeAttrsLabelIterator() {

    final List<PLabel> labels = createAttrLabels();

    return new LabelIterator<PLabel>() {

      Iterator<PLabel> it = null;
      int attrIndex = 0;
      double pos;

      public double getItemPosition() {
        return pos;
      }

      public double getItemSize() {
        return heatmapNode.getCellWidth();
      }

      public boolean hasNext() {
        return it.hasNext();
      }

      public PLabel next() {
        PLabel label = it.next();
        pos =
          heatmapNode.getBoundsReference().getX() +
          attrIndex * (heatmapNode.getCellWidth() + heatmapNode.getCellSpacing());

        attrIndex++;
        return label;
      }

      public void reset() {
        pos = Double.NaN;
        attrIndex = 0;
        it = labels.iterator();
      }

    };
  }

  private List<PLabel> createAttrLabels() {
    List<PLabel> labels = Lists.newArrayList();

    for (String attr : getFlowstratesView().getEdgeWeightAttrs()) {
      PLabel label = new PLabel(attr, new Insets(4, 3, 1, -3));
      label.setLabelBackground(LABEL_BACKGROUND);
      label.setName(attr);
      label.setFont(ATTR_LABELS_FONT);
      label.setPaint(Color.white);
      label.rotateInPlace(-Math.PI / 4);
      labels.add(label);

      label.addInputEventListener(new PTypedBasicInputEventHandler<PLabel>(PLabel.class) {
        @Override
        public void mouseEntered(PInputEvent event) {
          PLabel label = node(event);
          label.moveToFront();
          final String attr = label.getName();

          updateMapsOnHeatmapColumnHover(attr, true);

//          columnHighlightRect.setBounds(
//              GeomUtils.growRect(PNodes.fullBoundsOf(cells), 2));
//          columnHighlightRect.moveToFront();
//          columnHighlightRect.setVisible(true);
//          columnHighlightRect.repaint();

          FlowstratesView fs = getFlowstratesView();
          fs.getFlowLinesLayerNode().hideAllFlowLines();

          cursor.setHighlightedColumn(fs.getEdgeWeightAttrIndex(attr));
        }

        @Override
        public void mouseClicked(PInputEvent event) {
          final String attr = node(event).getName();
          FlowstratesView fs = getFlowstratesView();


          if (event.isControlDown()) {
            fs.setRowOrdering(new RowOrdering() {
              @Override
              public Comparator<Edge> getComparator(FlowstratesView fs) {
                return Collections.reverseOrder(
                    fs.getFlowMapGraph().createMaxEdgeWeightComparator(attr));
              }
            });
          } else {
            fs.getMapLayer(FlowEndpoint.ORIGIN).focusOnNodesOfVisibleEdges();
            fs.getMapLayer(FlowEndpoint.DEST).focusOnNodesOfVisibleEdges();
          }

        }

        @Override
        public void mouseExited(PInputEvent event) {
          PLabel label = node(event);
//          columnHighlightRect.setVisible(false);
          updateMapsOnHeatmapColumnHover(label.getName(), false);

          getFlowstratesView().getFlowLinesLayerNode().updateFlowLines();

          cursor.setHighlightedColumn(-1);
        }
      });

    }
    return labels;
  }

  private LabelIterator<String> createNodeLabelIterator(final FlowEndpoint ep) {
    return new LabelIterator<String>() {

      Iterator<String> it = null;
      int index = 0;
      double pos;

      public double getItemPosition() {
        return pos;
      }

      public double getItemSize() {
        return heatmapNode.getCellHeight();
      }

      public boolean hasNext() {
        return it.hasNext();
      }

      public String next() {
        String label = it.next();
        pos = calcNodeLabelYPos(index);

        index++;
        return label;
      }

      public void reset() {
        pos = Double.NaN;
        index = 0;
        it = Iterators.transform(
            getNodeLabels(ep).iterator(),
            new Function<String, String>() {

              @Override
              public String apply(String label) {
                if (label == null) return null;
                return FlowstratesView.shortenNodeLabel(label);
              }
            });
      }

    };
  }


}