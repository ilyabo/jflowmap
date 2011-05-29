package jflowmap.util.piccolo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class PLabel extends PNode {

  private static final Color DEFAULT_LABEL_BG = Color.white; // new Color(153, 153, 153);
  private Color labelBackground = DEFAULT_LABEL_BG; // new Color(153, 153, 153);
  private static final Color SEL_LABEL_BG = new Color(215, 48, 39);
  private static final Color PRESSED_SEL_LABEL_BG = new Color(225, 58, 49);
  private static final Color NON_SEL_LABEL_FG = Color.black;
  private static final Color SEL_LABEL_FG = Color.white;

  private static final Font FONT = new Font("Arial", Font.BOLD, 13);

  private final PText textNode;
  private final PPath rectNode;
  private final Insets pad;
  private final float arcWidth = 5;
  private final float arcHeight = 5;

  public PLabel(String text) {
    this(text, new Insets(5, 5, 5, 5));
  }

  public PLabel(String text, Insets pad) {
    this.textNode = new PText(text);
    textNode.setFont(FONT);
    textNode.setPickable(false);
    this.pad = pad;
    PBounds textb = textNode.getFullBoundsReference();
    this.rectNode = PPath.createRoundRectangle(
        -pad.left, -pad.top, (float) textb.getWidth() + pad.left + pad.right,
        (float) textb.getHeight() + pad.top + pad.bottom, arcWidth, arcHeight
    );

    textNode.setTextPaint(NON_SEL_LABEL_FG);

    rectNode.setStroke(null);
    addChild(rectNode);
    addChild(textNode);
    onBoundsChanged();

    addInputEventListener(new PTypedBasicInputEventHandler<PLabel>(PLabel.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        PLabel label = node(event);
        label.getTextNode().setTextPaint(SEL_LABEL_FG);
        label.getRectNode().setPaint(SEL_LABEL_BG);
        label.moveToFront();
        repaint();
      }
      @Override
      public void mouseExited(PInputEvent event) {
        PLabel label = node(event);
        label.getTextNode().setTextPaint(NON_SEL_LABEL_FG);
        label.getRectNode().setPaint(labelBackground);
        repaint();
      }
      @Override
      public void mousePressed(PInputEvent event) {
        PLabel label = node(event);
        label.getRectNode().setPaint(PRESSED_SEL_LABEL_BG);
        repaint();
      }
      @Override
      public void mouseReleased(PInputEvent event) {
        PLabel label = node(event);
        label.getRectNode().setPaint(SEL_LABEL_BG);
        repaint();
      }
    });
  }

  public void setLabelBackground(Color labelBackground) {
    this.labelBackground = labelBackground;
    rectNode.setPaint(labelBackground);
  }

  @Override
  public boolean setBounds(double x, double y, double width, double height) {
    boolean rv = super.setBounds(x, y, width, height);
    if (rv) {
      onBoundsChanged();
    }
    return rv;
  }

  @Override
  public void setPickable(boolean isPickable) {
    super.setPickable(isPickable);
    rectNode.setPickable(isPickable);
    textNode.setPickable(false);  // (otherwise it produces unnecessary mousein/out events)
  }

  private void onBoundsChanged() {
    textNode.setBounds(getBoundsReference());
    rectNode.setBounds(
        -pad.left + textNode.getX(),
        -pad.top + textNode.getY(),
        (float) textNode.getWidth() + pad.left + pad.right,
        (float) textNode.getHeight() + pad.top + pad.bottom);
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