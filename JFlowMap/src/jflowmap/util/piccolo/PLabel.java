package jflowmap.util.piccolo;

import java.awt.Color;
import java.awt.Font;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * @author Ilya Boyandin
 */
public class PLabel extends PNode {

  private static final Color NON_SEL_LABEL_BG = Color.white; // new Color(153, 153, 153);
  private static final Color SEL_LABEL_BG = new Color(215, 48, 39);
  private static final Color PRESSED_SEL_LABEL_BG = new Color(225, 58, 49);
  private static final Color NON_SEL_LABEL_FG = Color.black;
  private static final Color SEL_LABEL_FG = Color.white;

  private static final Font FONT = new Font("Arial", Font.BOLD, 13);

  private final PText textNode;
  private final PPath rectNode;
  private int pad = 5;
  private final float arcWidth = 5;
  private final float arcHeight = 5;

  public PLabel(String text) {
    this.textNode = new PText(text);
    textNode.setFont(FONT);
    textNode.setPickable(false);
    pad = 5;
    this.rectNode = PPath.createRoundRectangle(
        -pad, -pad, (float) textNode.getWidth() + 2 * pad,
        (float) textNode.getHeight() + 2 * pad, arcWidth, arcHeight
        );

    rectNode.setPaint(NON_SEL_LABEL_BG);
    textNode.setTextPaint(NON_SEL_LABEL_FG);

    rectNode.setStroke(null);
    addChild(rectNode);
    addChild(textNode);
    onBoundsChanged();

    addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseEntered(PInputEvent event) {
        PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
        if (label != null) {
          label.getTextNode().setTextPaint(SEL_LABEL_FG);
          label.getRectNode().setPaint(SEL_LABEL_BG);
          repaint();
        }
      }
      @Override
      public void mouseExited(PInputEvent event) {
        PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
        if (label != null) {
          label.getTextNode().setTextPaint(NON_SEL_LABEL_FG);
          label.getRectNode().setPaint(NON_SEL_LABEL_BG);
          repaint();
        }
      }
      @Override
      public void mousePressed(PInputEvent event) {
        PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
        if (label != null) {
          label.getRectNode().setPaint(PRESSED_SEL_LABEL_BG);
          repaint();
        }
      }
      @Override
      public void mouseReleased(PInputEvent event) {
        PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
        if (label != null) {
          label.getRectNode().setPaint(SEL_LABEL_BG);
          repaint();
        }
      }
    });
  }

  @Override
  public boolean setBounds(double x, double y, double width, double height) {
    boolean rv = super.setBounds(x, y, width, height);
    if (rv) {
      onBoundsChanged();
    }
    return rv;
  }

  private void onBoundsChanged() {
    textNode.setBounds(getBoundsReference());
    rectNode.setBounds(
        -pad + textNode.getX(),
        -pad + textNode.getY(),
        (float) textNode.getWidth() + 2 * pad,
        (float) textNode.getHeight() + 2 * pad);
  }

  public PText getTextNode() {
    return textNode;
  }

  public PPath getRectNode() {
    return rectNode;
  }

  public void setFont(Font font) {
    textNode.setFont(font);
  }
}
