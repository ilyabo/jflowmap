package jflowmap.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jflowmap.FlowMapAttrSpec;
import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractFlowMapStats implements FlowMapStats {

  protected static final String NODE_ATTR_KEY_PREFIX = "NODE_";
  protected static final String EDGE_ATTR_KEY_PREFIX = "EDGE_";

  private final Map<String, MinMax> statsCache = new HashMap<String, MinMax>();

  private final FlowMapAttrSpec attrSpec;

  public AbstractFlowMapStats(FlowMapAttrSpec attrSpec) {
    this.attrSpec = attrSpec;
  }

  protected synchronized MinMax getCachedOrCalc(String key, AttrStatsCalculator calc) {
    MinMax stats = statsCache.get(key);
    if (stats == null) {
      stats = calc.calc();
      statsCache.put(key, stats);
    }
    return stats;
  }

  protected interface AttrStatsCalculator {
    MinMax calc();
  }

  protected abstract Iterable<Edge> edges();

  public MinMax getEdgeLengthStats() {
    return getCachedOrCalc(
        EDGE_ATTR_KEY_PREFIX + "LENGTH",
        new AttrStatsCalculator() {
          @Override
          public MinMax calc() {
            List<Double> edgeLengths = Lists.newArrayList();
            for (Edge edge : edges()) {
              Node src = edge.getSourceNode();
              Node target = edge.getTargetNode();
              double x1 = src.getDouble(attrSpec.getXNodeAttr());
              double y1 = src.getDouble(attrSpec.getYNodeAttr());
              double x2 = target.getDouble(attrSpec.getXNodeAttr());
              double y2 = target.getDouble(attrSpec.getYNodeAttr());
              edgeLengths.add(Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
            }
            return MinMax.createFor(edgeLengths.iterator());
          }
        });
  }


  protected MinMax getEdgeAttrsStats(String key, final List<String> edgeAttrs) {
    return getCachedOrCalc(
        key,
        new AttrStatsCalculator() {
          @Override
          public MinMax calc() {
            return TupleStats.createFor(edges(), edgeAttrs);
          }
        }
    );
  }

}
