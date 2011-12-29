package jflowmap.util.piccolo;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class PPaths {

  private PPaths() {
  }

  public static PPath rect(double x, double y, double w, double h) {
    return rect(x, y, w, h, Color.black, Color.white);
  }

  public static PPath rect(double x, double y, double w, double h, Paint stroke, Paint fill) {
    PPath rect = new PPath(new Rectangle2D.Double(x, y, w, h));
    rect.setPaint(fill);
    rect.setStrokePaint(stroke);
    return rect;
  }

}
