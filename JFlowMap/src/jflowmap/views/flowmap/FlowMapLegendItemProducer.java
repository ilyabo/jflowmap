package jflowmap.views.flowmap;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import jflowmap.data.SeqStat;
import jflowmap.geom.ArrowQuadPath;
import jflowmap.views.ColorCodes;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class FlowMapLegendItemProducer extends AbstractLegendItemProducer {

  private final double lineWidth = 50;
  private final double spaceForHeader = 10;
  private final VisualFlowMap visualFlowMap;

  public FlowMapLegendItemProducer(VisualFlowMap visualFlowMap, int numLegendValues) {
    super(numLegendValues);
    this.visualFlowMap = visualFlowMap;
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
  public SeqStat getValueStat() {
    return visualFlowMap.getValueStat();
  }

  @Override
  public PNode createItem(final double value) {
    double x = 0, y =0;

    VisualFlowMapModel fmm = visualFlowMap.getModel();

    Stroke stroke = visualFlowMap.getVisualEdgeStrokeFactory().createStroke(
        fmm.normalizeForWidthScale(value));

    double sw = getStrokeWidth(stroke);
    double yp = y + sw/2;
    PPath ppath = new PPath(new Line2D.Double(x, yp, x + lineWidth, yp)) {
      @Override
      public boolean setBounds(double x, double y, double width, double height) {
        boolean rv = super.setBounds(x, y, width, height);
        if (rv) {
          VisualFlowMapModel fmm = visualFlowMap.getModel();
          setStrokePaint(visualFlowMap.getVisualEdgePaintFactory().createPaint(
              fmm.normalizeForColorScale(value), x, y, x + width, y));
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
