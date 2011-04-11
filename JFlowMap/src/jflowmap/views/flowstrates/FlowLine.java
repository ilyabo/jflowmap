package jflowmap.views.flowstrates;

import java.awt.BasicStroke;
import java.awt.Color;

import edu.umd.cs.piccolox.nodes.PLine;

/**
 * @author Ilya Boyandin
 */
class FlowLine extends PLine {
  private static final BasicStroke STROKE = new BasicStroke(2);

  private boolean isHighlighted;
  private Color color = Color.white;
  private Color highlightedColor = Color.yellow;

  public FlowLine() {
//    this.color = color;
//    this.highlightedColor = highlightedColor;
    setStroke(STROKE);
    for (int i = 0; i < 3; i++) {
      addPoint(i, 0, 0);
    }
    updateColor();
  }

  public boolean isHighlighted() {
    return isHighlighted;
  }

  public void setHighlighted(boolean isHighlighted) {
    if (this.isHighlighted != isHighlighted) {
      this.isHighlighted = isHighlighted;
      updateColor();
    }
  }

  private void updateColor() {
    if (isHighlighted()) {
      setStrokePaint(getHighlightedColor());
    } else {
      setStrokePaint(getColor());
    }
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
    updateColor();
  }

  public void setHighlightedColor(Color highlightedColor) {
    this.highlightedColor = highlightedColor;
    updateColor();
  }

  public Color getHighlightedColor() {
    return highlightedColor;
  }
}
