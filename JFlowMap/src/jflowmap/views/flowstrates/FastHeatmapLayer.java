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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import prefuse.data.Edge;
import prefuse.data.Node;
import at.fhjoanneum.cgvis.data.IColorForValue;
import at.fhjoanneum.cgvis.data.IDataValues;
import at.fhjoanneum.cgvis.plots.FloatingLabelsNode;
import at.fhjoanneum.cgvis.plots.FloatingLabelsNode.LabelIterator;
import at.fhjoanneum.cgvis.plots.mosaic.MosaicPlotNode;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class FastHeatmapLayer extends TemporalViewLayer {

  private static final int MAX_NODE_NAME_LENGTH = 18;
  private MosaicPlotNode mosaicPlotNode;
  private final IColorForValue colorForValue;
  private final FloatingLabelsNode attrLabelsNode;
  private final FloatingLabelsNode originLabelsNode;

  PPath ppath;
  private final FloatingLabelsNode destLabelsNode;

  public FastHeatmapLayer(FlowstratesView flowstratesView) {
    super(flowstratesView);
    colorForValue = createColorForValue();


    renew();

    getCamera().setComponent(getFlowstratesView().getVisualCanvas());

    originLabelsNode = new FloatingLabelsNode(false, createNodeLabelIterator(FlowEndpoint.ORIGIN));
    originLabelsNode.setAnchorLabelsToEnd(true);
    destLabelsNode = new FloatingLabelsNode(false, createNodeLabelIterator(FlowEndpoint.DEST));
    originLabelsNode.setAnchorLabelsToEnd(false);
    attrLabelsNode = new FloatingLabelsNode(true, createAttrsLabelIterator());
    getCamera().addChild(originLabelsNode);
    getCamera().addChild(destLabelsNode);
    getCamera().addChild(attrLabelsNode);

    originLabelsNode.addDisjointNode(attrLabelsNode);
    destLabelsNode.addDisjointNode(attrLabelsNode);
  }

  @Override
  public void renew() {
    removeAllChildren();
//    getCamera().removeAllChildren();

    IDataValues data = getDataValues();
    FlowstratesView fs = getFlowstratesView();
    mosaicPlotNode = new MosaicPlotNode(data, fs.getVisualCanvas(), colorForValue);

    Color missingColor = fs.getStyle().getMissingValueColor();
    mosaicPlotNode.setMissingValueColor1(missingColor);
    mosaicPlotNode.setMissingValueColor2(missingColor);

    addChild(mosaicPlotNode);
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
    renew();
  }

  @Override
  public void resetWeightAttrTotals() {
  }

  @Override
  public void fitInView() {
    HeatmapLayer.fitBoundsInCameraView(mosaicPlotNode.getFullBounds(), getCamera());
  }

  @Override
  public Rectangle2D getEdgeLabelBounds(Edge edge, FlowEndpoint ep) {
    return null;
  }

  @Override
  public Point2D getFlowLineInPoint(int row, FlowEndpoint ep) {
    return null;
  }

  private LabelIterator createAttrsLabelIterator() {
    return new LabelIterator() {

      Iterator<String> it = null;
      int attrIndex = 0;
      double pos;

      public double getPosition() {
        return pos;
      }

      public double getSize() {
        return mosaicPlotNode.getCellWidth();
      }

      public boolean hasNext() {
        return it.hasNext();
      }

      public String next() {
        String label = it.next();
        pos =
          mosaicPlotNode.getBoundsReference().getX() +
          attrIndex * (mosaicPlotNode.getCellWidth() + mosaicPlotNode.getCellSpacing());

        attrIndex++;
        return label;
      }

      public void reset() {
        pos = Double.NaN;
        attrIndex = 0;
        it = attrs().iterator();
      }

      private List<String> attrs() {
        return getFlowstratesView().getFlowMapGraph().getEdgeWeightAttrs();
      }
    };
  }

  private LabelIterator createNodeLabelIterator(final FlowEndpoint ep) {
    return new LabelIterator() {

      Iterator<String> it = null;
      int index = 0;
      double pos;

      public double getPosition() {
        return pos;
      }

      public double getSize() {
        return mosaicPlotNode.getCellHeight();
      }

      public boolean hasNext() {
        return it.hasNext();
      }

      public String next() {
        String label = it.next();
        pos =
          mosaicPlotNode.getBoundsReference().getY() +
          index * (mosaicPlotNode.getCellHeight() + mosaicPlotNode.getCellSpacing());

        index++;
        return label;
      }

      public void reset() {
        pos = Double.NaN;
        index = 0;
        List<Edge> edges = getFlowstratesView().getVisibleEdges();
        final FlowMapGraph fmg = getFlowstratesView().getFlowMapGraph();
        it = Iterators.transform(edges.iterator(), new Function<Edge, String>() {
          @Override
          public String apply(Edge e) {
            Node node = ep.nodeOf(e);
            String name = fmg.getNodeLabel(node);
            if (name.length() > MAX_NODE_NAME_LENGTH) {
              name = name.substring(0, MAX_NODE_NAME_LENGTH - 2) + ".";
            }
            return name;
          }
        });
      }
    };
  }




}