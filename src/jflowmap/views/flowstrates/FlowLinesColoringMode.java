package jflowmap.views.flowstrates;

import jflowmap.FlowEndpoint;

/**
 * @author Ilya Boyandin
 */
enum FlowLinesColoringMode {

  SAME_COLOR, ORIGIN, DEST;

  public static FlowLinesColoringMode of(FlowEndpoint endpoint) {
    switch (endpoint) {
    case ORIGIN: return ORIGIN;
    case DEST: return DEST;
    }
    throw new AssertionError();
  }

}