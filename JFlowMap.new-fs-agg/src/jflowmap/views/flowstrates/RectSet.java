package jflowmap.views.flowstrates;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

class RectSet {
  private final List<Rectangle2D> rects;

  public RectSet(int initialCapacity) {
    rects = new ArrayList<Rectangle2D>(initialCapacity);
  }

  public void add(Rectangle2D rect) {
    rects.add(rect);
  }

  public boolean intersects(Rectangle2D rect) {
    for (Rectangle2D r : rects) {
      if (r.intersects(rect)) {
        return true;
      }
    }
    return false;
  }

  public boolean addIfNotIntersects(Rectangle2D rect) {
    if (intersects(rect)) {
      return false;
    } else {
      add(rect);
      return true;
    }
  }
}