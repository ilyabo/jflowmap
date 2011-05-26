package jflowmap.views.flowstrates;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jflowmap.util.piccolo.PLabel;
import at.fhjoanneum.cgvis.plots.AbstractFloatingLabelsNode;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class InteractiveFloatingLabelsNode extends AbstractFloatingLabelsNode<PLabel> {

  public InteractiveFloatingLabelsNode(boolean isHorizontal, LabelIterator<PLabel> it) {
    super(isHorizontal, it);

    it.reset();
    while (it.hasNext()) {
      PLabel label = it.next();
      addChild(label);
    }
  }

  @Override
  public void setParent(PNode parent) {
    if (!(parent instanceof PCamera)) {
      throw new IllegalArgumentException(getClass().getSimpleName() + "'s parent must be PCamera");
    }
    super.setParent(parent);
    parent.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        positionLabels();
      }
    });

  }

  @Override
  public boolean setBounds(double x, double y, double width, double height) {
    boolean rv = super.setBounds(x, y, width, height);
    positionLabels();
    return rv;
  }
  @Override
  protected double getLabelWidth(int index, PLabel label) {
    return label.getTextNode().getFullBoundsReference().getHeight() * 2.5;
  }

  @Override
  protected double getLabelHeight(int index, PLabel label) {
    return label.getTextNode().getFullBoundsReference().getWidth() / 1.5;
  }

  private void positionLabels() {
    positionLabels(new LabelPositioner<PLabel>() {

      @Override
      public void showSpacer(int x, int y) {
        // TODO: maybe show a "..." node somewhere
      }

      @Override
      public void showLabel(PLabel label, int index, int x, int y) {
        PBounds fb = label.getFullBoundsReference();
        label.setOffset(x - fb.width/2, getBoundsReference().getMaxY() - fb.height*0.37);
        label.setVisible(true);
      }

      @Override
      public void hideLabel(PLabel label, int count) {
        label.setVisible(false);
      }

    });
  }

}
