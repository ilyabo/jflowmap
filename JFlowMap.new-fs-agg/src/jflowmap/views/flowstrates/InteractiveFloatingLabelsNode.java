package jflowmap.views.flowstrates;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jflowmap.util.piccolo.PLabel;
import at.fhjoanneum.cgvis.plots.AbstractFloatingLabelsNode;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;

/**
 * @author Ilya Boyandin
 */
public class InteractiveFloatingLabelsNode extends AbstractFloatingLabelsNode<PLabel> {

  private final LabelPositioner<PLabel> labelPositioner;

  public InteractiveFloatingLabelsNode(boolean isHorizontal,
      LabelIterator<PLabel> it, LabelPositioner<PLabel> labelPositioner) {
    super(isHorizontal, it);

    this.labelPositioner = labelPositioner;

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

  public void positionLabels() {
    positionLabels(labelPositioner);
  }

}
