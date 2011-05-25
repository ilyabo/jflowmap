package jflowmap.views.flowstrates;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jflowmap.util.piccolo.PLabel;
import jflowmap.util.piccolo.PNodes;
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
//    final PCamera camera = (PCamera)parent;
    parent.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        positionLabels(new LabelPositioner<PLabel>() {

          @Override
          public void showSpacer(int x, int y) {
            // TODO: maybe show a "..." node somewhere
          }

          @Override
          public void showLabel(PLabel label, int index, int x, int y) {
            PBounds fb = label.getFullBoundsReference();
            PNodes.setPosition(label, x - fb.getWidth()/2, getY() + (getHeight() - fb.getHeight())/2);
//            PBounds fb = label.getFullBoundsReference();
//            label.rotateAboutPoint(-Math.PI * .65 / 2   - label.getRotation(),
//                fb.getMinX(),
//                fb.getMaxY());
//            label.rotateInPlace(-Math.PI * .65 / 2   - label.getRotation());
            label.setVisible(true);
          }

          @Override
          public void hideLabel(PLabel label, int count) {
            label.setVisible(false);
          }

        });
      }
    });

  }

  @Override
  protected double getLabelWidth(int index, PLabel label) {
    return label.getFullBoundsReference().getWidth();
  }

  @Override
  protected double getLabelHeight(int index, PLabel label) {
    return label.getFullBoundsReference().getHeight();
  }

  @Override
  public boolean setBounds(double x, double y, double w, double h) {
    double dx = x - getX();
    double dy = y - getY();
    PNodes.moveChildrenBy(this, dx, dy);
    return super.setBounds(x, y, w, h);
  }
}
