package jflowmap.visuals.timeline;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import at.fhjoanneum.cgvis.plots.AbstractFloatingPanelNode;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class FloatingLabelsNode extends AbstractFloatingPanelNode {

  private static final long serialVersionUID = -7841682728936489973L;
  private static Font LABELS_FONT = new Font("Helvetica", Font.PLAIN, 9);
  private static final Color BG_COLOR = new Color(255, 255, 255, 190);
  private static final Color TEXT_COLOR = Color.black;
  private static final int MARGIN_BEFORE = 5;
  private static final int MARGIN_AFTER = 2;

  private final LabelIterator labelIterator;
  private int contentLength;

  // private boolean wasSizeAdjustedToLabels;

  public FloatingLabelsNode(boolean isHorizontal, LabelIterator it) {
    super(isHorizontal);
    this.labelIterator = it;

    setPaint(BG_COLOR);
  }

  @Override
  public void setParent(PNode parent) {
    super.setParent(parent);
    adjustSizeToLabels();
  }

  private void adjustSizeToLabels() {
    final FontMetrics fm = getCanvas().getFontMetrics(LABELS_FONT);
    final LabelIterator it = labelIterator;
    it.reset();
    int maxw = 0;
    double lastPos = 0, firstPos = 0, lastLen = 0, firstLen = 0;
    int count = 0;
    while (it.hasNext()) {
      final String label = it.next();
      final int w = fm.stringWidth(label);
      if (w > maxw)
        maxw = w;

      if (count == 0) {
        firstPos = it.getPosition();
        firstLen = it.getSize();
      }
      lastPos = it.getPosition();
      lastLen = it.getSize();
      count++;
    }
    if (isHorizontal()) {
      setHeight(maxw + MARGIN_BEFORE + MARGIN_AFTER);
    } else {
      setWidth(maxw + MARGIN_BEFORE + MARGIN_AFTER);
    }
    contentLength = (int) Math.ceil(lastPos - firstPos
        + (lastLen + firstLen) / 2);
  }

  @Override
  protected void paintContent(Graphics2D g2, int offsetX, int offsetY) {
    final PCamera camera = getCamera();
    final PBounds viewBounds = camera.getViewBounds();
    final PBounds bounds = getBoundsReference();

    g2.setFont(LABELS_FONT);
    final FontMetrics fm = g2.getFontMetrics();

    final int fAscent = fm.getAscent();
    // final int fHeight = fm.getHeight();
    g2.setColor(TEXT_COLOR);

    final LabelIterator it = labelIterator;
    it.reset();
    if (isHorizontal()) {
      final Point2D.Double pos = new Point2D.Double(0, 0);
      final double scale = bounds.getWidth() / viewBounds.getWidth();

      g2.rotate(-Math.PI / 2, 0, 0);

      int count = 0;
      int prevx = 0;
      int skipCount = 0;
      boolean dots = false;

      final int y = (int) (-bounds.getY() - bounds.getHeight());
      final int size = (int) Math.round(it.getSize() * scale);

      while (it.hasNext()) {
        final String label = it.next();

        final double posx = it.getPosition();
        if (viewBounds.getMaxX() <= posx) {
          break;
        }
        final boolean doDraw = (viewBounds.getMinX() < posx
            + it.getSize() * 2);

        pos.x = posx;
        pos.y = 0;
        camera.viewToLocal(pos);

        final int x = (int) (pos.x + (size + fAscent) / 2) - 1;

        if (count == 0 || x - prevx >= fAscent) {
          if (skipCount > 0 && !dots) {
            final int _x = x - fAscent / 2 + 2;
            if (doDraw) {
              g2.drawString("...", y + MARGIN_BEFORE, _x);
            }
            skipCount++;
            dots = true;
          } else {
            if (doDraw) {
              g2.drawString(label, y + MARGIN_BEFORE, x);
            }
            dots = false;
          }
          prevx = x;
        } else {
          skipCount++;
        }
        count++;
      }
      g2.rotate(Math.PI / 2, 0, 0);
    } else {
      final Point2D.Double pos = new Point2D.Double(0, 0);
      final double scale = bounds.getHeight() / viewBounds.getHeight();

      int count = 0;
      int prevy = 0;
      int skipCount = 0;
      boolean dots = false;

      final int x = (int) bounds.getX();
      final int size = (int) Math.round(it.getSize() * scale);

      while (it.hasNext()) {
        final String label = it.next();

        final double posy = it.getPosition();
        if (viewBounds.getMaxY() <= posy) {
          break;
        }
        final boolean doDraw = (viewBounds.getMinY() < posy
            + it.getSize() * 2);

        pos.x = 0;
        pos.y = posy;
        camera.viewToLocal(pos);

        final int y = (int) (pos.y + (size + fAscent) / 2) - 1;

        if (count == 0 || y - prevy >= fAscent) {
          if (skipCount > 0 && !dots) {
            final int _y = y - fAscent / 2 + 2;
            if (doDraw) {
              g2.drawString("...", x + MARGIN_BEFORE, _y);
            }
            skipCount++;
            dots = true;
          } else {
            if (doDraw) {
//              g2.setColor(it.getColor());
//              g2.fill(new Rectangle2D.Double(x, y, bounds.getWidth(), size));
              g2.setColor(TEXT_COLOR);
              g2.drawString(label, x + MARGIN_BEFORE, y);
            }
            dots = false;
          }
          prevy = y;
        } else {
          skipCount++;
        }
        count++;
      }
    }
  }

  public interface LabelIterator {
    boolean hasNext();

    String next();

    double getPosition();

    double getSize();

    Color getColor();

    void reset();
  }

  @Override
  protected int getContentWidth() {
    if (isHorizontal()) {
      final double viewScale = getCamera().getViewScale();
      return (int) Math.ceil(contentLength * viewScale);
    } else {
      return (int) Math.ceil(getWidth());
    }
  }

  @Override
  protected int getContentHeight() {
    if (isHorizontal()) {
      return (int) Math.ceil(getHeight());
    } else {
      final double viewScale = getCamera().getViewScale();
      return (int) Math.ceil(contentLength * viewScale);
    }
  }

}
