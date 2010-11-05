package jflowmap.views.flowstrates;

import java.util.List;

import jflowmap.FlowMapGraph;
import jflowmap.util.BagOfWordsFilter;
import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author Ilya Boyandin
 */
public class FlowstratesHeatmapRowFilters {

  private FlowstratesHeatmapRowFilters() {
  }

  public static Predicate<Edge> createEdgeFilter_bySrcAndTargetNodeIds( final FlowMapGraph fmg,
      final List<String> srcIds, final List<String> targetIds) {
    if (srcIds == null  &&  targetIds == null) {
      return null;
    }
    return new Predicate<Edge>() {
      @Override
      public boolean apply(Edge edge) {
        return
          (srcIds == null  ||  srcIds.contains(fmg.getNodeId(edge.getSourceNode())))  &&
          (targetIds == null  ||  targetIds.contains(fmg.getNodeId(edge.getTargetNode())));
      }
    };
  }

  public static Predicate<Edge> createEdgeFilter_bySrcTargetNamesAsBagOfWords(final FlowMapGraph fmg,
        String srcQuery, String targetQuery) {
      final List<String[]> srcQueryWordGroups = BagOfWordsFilter.wordGroups(srcQuery.toLowerCase());
      final List<String[]> targetQueryWordGroups = BagOfWordsFilter.wordGroups(targetQuery.toLowerCase());
      return new Predicate<Edge>() {
        @Override
        public boolean apply(Edge edge) {
          Node srcNode = edge.getSourceNode();
          Node targetNode = edge.getTargetNode();

          final String srcNames = fmg.getNodeLabel(srcNode);
          final String targetNames = fmg.getNodeLabel(targetNode);

          return
            (srcQueryWordGroups.isEmpty()  ||
             Iterables.any(srcQueryWordGroups, new Predicate<String[]>() {
              @Override
              public boolean apply(String[] srcQueryWords) {
                return BagOfWordsFilter.ALL.apply(srcNames, srcQueryWords);
              }
            }))
              &&
            (targetQueryWordGroups.isEmpty()  ||
             Iterables.any(targetQueryWordGroups, new Predicate<String[]>() {
              @Override
              public boolean apply(String[] targetQueryWords) {
                return BagOfWordsFilter.ALL.apply(targetNames, targetQueryWords);
              }
            }));

  //          BagOfWordsFilter.ALL.apply(srcNames, srcQueryWords)   &&
  //          BagOfWordsFilter.ALL.apply(targetNames, targetQueryWords);
        }
      };
    }

}
