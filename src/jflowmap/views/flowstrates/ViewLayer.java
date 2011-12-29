package jflowmap.views.flowstrates;

import edu.umd.cs.piccolo.PCamera;

/**
 * @author Ilya Boyandin
 */
public interface ViewLayer {

  void fitInView(boolean animate, boolean whole);

  PCamera getCamera();

}
