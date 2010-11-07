package jflowmap.data;

import java.util.List;
import java.util.Map;

import jflowmap.FlowMapAttrSpec;
import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractFlowMapStats implements FlowMapStats {

  protected enum AttrKeys {
    NODE_X, NODE_Y,
    EDGE_LENGTH, EDGE_WEIGHT, EDGE_WEIGHT_DIFF, EDGE_WEIGHT_DIFF_REL
    ;

    public static String nodeAttr(String attrName) {
      return NODE_ATTR_KEY_PREFIX + attrName;
    }

    public static String edgeAttr(String attrName) {
      return EDGE_ATTR_KEY_PREFIX + attrName;
    }
  }

  private static final String NODE_ATTR_KEY_PREFIX = ":node:";
  private static final String EDGE_ATTR_KEY_PREFIX = ":edge:";

  private final Map<String, MinMax> statsCache = Maps.newHashMap();

  private final FlowMapAttrSpec attrSpec;

  public AbstractFlowMapStats(FlowMapAttrSpec attrSpec) {
    this.attrSpec = attrSpec;
  }

  public FlowMapAttrSpec getAttrSpec() {
    return attrSpec;
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
        AttrKeys.EDGE_LENGTH.name(),
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

}
