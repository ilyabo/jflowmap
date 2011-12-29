package jflowmap.util.piccolo;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PClip;

/**
 * @author Ilya Boyandin
 */
public class PCollapsableItemsContainer extends PNode {

  private static final double itemLabelSpacing = 5;
  private static final double itemItemSpacing = 5;
  private static final double itemBodySpacing = 5;
  private static final int collapseAnimationDuration = 200;

  private final boolean collapsedByDefault;
  private final CollapseHandler collapseHandler;
  private double labelsWidth;

  public PCollapsableItemsContainer() {
    this(true);
  }

  public PCollapsableItemsContainer(boolean collapsedByDefault) {
    this.collapsedByDefault = collapsedByDefault;
    this.collapseHandler = new CollapseHandler();
  }

  public void layoutItems() {
    double labelMaxX = 0;
    for (Item item : PNodes.childrenOfType(this, Item.class)) {
      PBounds lb = item.getLabel().getFullBoundsReference();
      double mx = lb.getMaxX();
      if (mx > labelMaxX) labelMaxX = mx;
    }

    this.labelsWidth = labelMaxX;

    double accHeight = 0;
    for (Item item : PNodes.childrenOfType(this, Item.class)) {
      PNode head = item.getHead();
      PNode label = item.getLabel();
      PClip bodyClip = item.getBodyClip();

//      PNodes.moveTo(label, getX(), getY() + accHeight + itemItemSpacing);
      PNodes.moveTo(label, labelMaxX - label.getFullBoundsReference().getWidth(), getY() + accHeight + itemItemSpacing);
      PNodes.moveTo(head, getX() + labelMaxX + itemLabelSpacing, getY() + accHeight);
      PNodes.moveTo(bodyClip, getX() + labelMaxX + itemLabelSpacing, getY() + accHeight + head.getHeight() + itemBodySpacing);

      accHeight += Math.max(head.getHeight(), label.getHeight()) + itemItemSpacing;
    }

    repaint();
  }

  public double getItemsOffsetX() {
    return labelsWidth + itemLabelSpacing;
  }

  public Item addNewItem(String labelText, PNode head, PNode body) {
    return addNewItem(createLabel(labelText), head, body);
  }

  public Item addNewItem(PNode label, PNode head, PNode body) {
    Item item = new Item(label, head, body, collapsedByDefault);
    addChild(item);
    return item;
  }

  public void toggleCollapsed(Item item) {
    int index = PNodes.indexOfChild(this, item);
    if (index >= 0) {
      // shift subsequent items
      for (int i = index + 1, numChildren = getChildrenCount(); i < numChildren; i++) {
        PNode child = getChild(i);
        if (child instanceof Item) {
          final Item it = (Item)child;
          it.terminateAnimationIfStepping();
          it.animateToTransform(item.shift(it.getTransform()), collapseAnimationDuration);
        }
      }
      item.setCollapsed(!item.isCollapsed());
    }
  }

  public Item findItemByLabel(PNode label) {
    for (Item item : PNodes.childrenOfType(this, Item.class)) {
      if (item.getLabel() == label) return item;
    }
    return null;
  }

  public class Item extends PNode {
    private final PNode label;
    private final PNode head;
    private final PClip bodyClip;
//    private final Rectangle2D clipRect;
    private final PNode body;

    private boolean collapsed;
    private PActivity lastActivity;

    private Item(PNode label, PNode head, PNode body, boolean collapsed) {
      this.label = label;
      this.head = head;
      this.body = body;
      if (label != null) {
        addChild(label);
      }
      if (head != null) {
        addChild(head);
      }
      this.bodyClip = new PClip();
      this.bodyClip.setStroke(null);

      PBounds bb = getBodyBounds();
      bodyClip.setPathToRectangle((float)bb.x, (float)bb.y, (float)bb.width, (float)bb.height);

      this.collapsed = collapsed;
      updateClip(false);

      if (body != null) {
        addChild(bodyClip);
        bodyClip.addChild(body);
      }
    }

    public PCollapsableItemsContainer getContainer() {
      return PCollapsableItemsContainer.this;
    }

    public PNode getLabel() {
      return label;
    }

    public PNode getHead() {
      return head;
    }

    public PNode getBody() {
      return body;
    }

    public PClip getBodyClip() {
      return bodyClip;
    }

    public boolean isCollapsed() {
      return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
      boolean oldCollapsed = this.collapsed;
      this.collapsed = collapsed;
      if (oldCollapsed != collapsed) {
        updateClip(true);
      }
    }

    public void toggleCollapsed() {
      PCollapsableItemsContainer.this.toggleCollapsed(this);
    }

    private void updateClip(boolean animate) {
      PBounds bb = getBodyBounds();
      if (collapsed) {
        bb.height = 0;
      }

      if (animate) {
        bodyClip.animateToBounds(bb.x, bb.y, bb.width, bb.height, collapseAnimationDuration);
      } else {
        bodyClip.setBounds(bb.x, bb.y, bb.width, bb.height);
      }
    }

    private void terminateAnimationIfStepping() {
      if (lastActivity != null && lastActivity.isStepping()) {
        lastActivity.terminate(PActivity.TERMINATE_AND_FINISH_IF_STEPPING);
        lastActivity = null;
      }
    }

    @Override
    public boolean addActivity(PActivity activity) {
      if (super.addActivity(activity)) {
        lastActivity = activity;
        return true;
      } else {
        return false;
      }
    }

    private PAffineTransform shift(PAffineTransform t) {
      PAffineTransform st = new PAffineTransform();
      st.setOffset(0, (collapsed ? +1 : -1) * (getBodyBounds().getHeight() + itemBodySpacing * 2));
      st.concatenate(t);
      return st;
    }


    private PBounds getBodyBounds() {
      PBounds fb = body.getFullBounds();
      for (PNode child : PNodes.childrenOf(body)) {
        fb.add(child.getBoundsReference());
      }
      return fb;
    }

  }

  public PLabel createLabel(String text) {
    PLabel label = new PLabel(text);
    label.addInputEventListener(collapseHandler);
    return label;
  }

  public class CollapseHandler extends PBasicInputEventHandler {
    @Override
    public void mouseClicked(PInputEvent event) {
      PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
      if (label != null) {
        findItemByLabel(label).toggleCollapsed();
      }
    }
  }


}
