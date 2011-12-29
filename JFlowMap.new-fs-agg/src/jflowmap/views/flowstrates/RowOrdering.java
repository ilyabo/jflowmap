package jflowmap.views.flowstrates;

import java.util.Comparator;

import prefuse.data.Edge;

/**
 * @author Ilya Boyandin
 */
public interface RowOrdering {

  Comparator<Edge> getComparator(FlowstratesView fs);

}
