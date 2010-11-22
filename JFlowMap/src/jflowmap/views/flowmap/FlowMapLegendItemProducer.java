package jflowmap.views.flowmap;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.List;

import jflowmap.data.MinMax;
import jflowmap.geom.ArrowQuadPath;
import jflowmap.views.ColorCodes;
import jflowmap.views.VisualLegend.ItemProducer;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class FlowMapLegendItemProducer implements ItemProducer {

  private final double lineWidth = 50;
  private final double spaceForHeader = 10;
  private final VisualFlowMap visualFlowMap;
  private final int numLegendValues;

  public FlowMapLegendItemProducer(VisualFlowMap visualFlowMap, int numLegendValues) {
    this.visualFlowMap = visualFlowMap;
    this.numLegendValues = numLegendValues;
  }

  @Override
  public PNode createHeader() {
    double posX = 0, posY = 0;
    PPath arrow = new PPath(new ArrowQuadPath(
        posX + 2, posY + spaceForHeader/2, posX + lineWidth - 4, posY + spaceForHeader/2,
        posX + lineWidth/2, posY + spaceForHeader/2, 4));
    arrow.setStrokePaint(visualFlowMap.getColor(ColorCodes.LEDGEND_ARROW));
    return arrow;
  }

  @Override
  public Iterable<PNode> createItems() {
    List<PNode> items = Lists.newArrayList();

    MinMax stats = visualFlowMap.getFlowMapGraph().getStats().getEdgeWeightStats();
    List<Double> values = LegendValuesGenerator.generateLegendValues(
        stats.getMin(), stats.getMax(), numLegendValues);
    for (Double value : values) {
      items.add(createItem(value));
    }

    return items;
  }

  private PNode createItem(final double value) {
    double x = 0, y =0;


    VisualFlowMapModel fmm = visualFlowMap.getModel();

    Stroke stroke = visualFlowMap.getVisualEdgeStrokeFactory().createStroke(
        fmm.normalizeEdgeWeightForWidthScale(value));

    double sw = getStrokeWidth(stroke);
    double yp = y + sw/2;
    PPath ppath = new PPath(new Line2D.Double(x, yp, x + lineWidth, yp)) {
      @Override
      public boolean setBounds(double x, double y, double width, double height) {
        boolean rv = super.setBounds(x, y, width, height);
        if (rv) {
          VisualFlowMapModel fmm = visualFlowMap.getModel();
          setStrokePaint(visualFlowMap.getVisualEdgePaintFactory().createPaint(
              fmm.normalizeEdgeWeightForColorScale(value), x, y, x + width, y));
        }
        return rv;
      }
    };
    ppath.setStroke(stroke);

    ppath.setName(FlowMapView.NUMBER_FORMAT.format(value));
    return ppath;
  }

  private double getStrokeWidth(Stroke stroke) {
    double strokeWidth;
    if (stroke instanceof PFixedWidthStroke) {
      strokeWidth = ((PFixedWidthStroke)stroke).getLineWidth();
    } else if (stroke instanceof BasicStroke) {
      strokeWidth = ((BasicStroke)stroke).getLineWidth();
    } else {
      strokeWidth = visualFlowMap.getModel().getMaxEdgeWidth();
    }
    return strokeWidth;
  }
}
