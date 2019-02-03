package jflowmap.views.flowstrates;

import java.awt.Color;
import java.util.List;

import jflowmap.FlowMapGraph;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PPaths;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
class CentroidTimeSlider extends PNode {

  private static final int MARGIN_TOP = 5;
  private final Centroid centroid;
  private final double width = 42;
  private final double height = 7;

  public CentroidTimeSlider(Centroid centroid) {
    this.centroid = centroid;

    FlowstratesView view = centroid.getView();
    FlowMapGraph fmg = view.getFlowMapGraph();
    List<String> columns = fmg.getEdgeWeightAttrs();

    int numCols = columns.size();
    if (numCols > 0) {
      double w = width / numCols;
      PBounds cb = centroid.getFullBoundsReference();
      double x = cb.getX() + (cb.getWidth() - width)/2;
      double y = cb.getMaxY() + 2;
      for (String attr : columns) {
        PPath rect = PPaths.rect(x, y, w, height);
        rect.setName(attr);
        rect.setStroke(null);
        rect.setPaint(Color.red);
        addChild(rect);
        x += w;
      }
    }
  }

  public void updateColors() {
    for (PPath rect : PNodes.childrenOfType(this, PPath.class)) {
//      FlowMapSummaries.getWeightSummary(node, weightAttrName, dir)
//      rect.setPaint(centroid.getView().getColorFor(centroid.getNodeId(), rect.getName()));
    }
  }

}
