package jflowmap.util;
import javax.swing.SwingUtilities;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class PAccordion extends PNode {

    private final boolean collapsedByDefault;
    private final double itemLabelSpacing = 5;
    private final double itemItemSpacing = 5;
    private final double itemBodySpacing = 5;
    private final int collapseAnimationDuration = 200;
    private volatile boolean laidOut = false;

    public PAccordion() {
        this(true);
    }

    public PAccordion(boolean collapsedByDefault) {
        this.collapsedByDefault = collapsedByDefault;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (!laidOut) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    layoutItems();
                    laidOut = true;
                }
            });
        }
    }


    public void layoutItems() {
//        setPickable(false);
//        setChildrenPickable(false);

        double labelMaxX = 0;
        for (Item item : PiccoloUtils.childrenOfType(this, Item.class)) {
            PBounds lb = item.getLabel().getFullBoundsReference();
            double mx = lb.getMaxX();
            if (mx > labelMaxX) labelMaxX = mx;
        }

        double accHeight = 0;
        for (Item item : PiccoloUtils.childrenOfType(this, Item.class)) {
            PNode head = item.getHead();
            PNode label = item.getLabel();
            PNode body = item.getBody();

            PiccoloUtils.moveNodeTo(label, getX(), getY() + accHeight + itemItemSpacing);
            PiccoloUtils.moveNodeTo(head, getX() + labelMaxX + itemLabelSpacing, getY() + accHeight);
            PiccoloUtils.moveNodeTo(body, getX() + labelMaxX + itemLabelSpacing, getY() + accHeight + head.getHeight() + itemBodySpacing);

            accHeight += Math.max(head.getHeight(), label.getHeight()) + itemItemSpacing;

            if (collapsedByDefault) {
//                    body.setVisible(false);
            }
        }

//        setPickable(true);
//        setChildrenPickable(true);
    }

    public Item addNewItem(PNode label, PNode head, PNode body) {
        Item item = new Item(label, head, body, collapsedByDefault);
        addChild(item);
        laidOut = false;
        return item;
    }

    public void toggleCollapsed(Item item) {
        int index = PiccoloUtils.indexOfChild(this, item);
        if (index >= 0) {
            for (int i = index + 1, numChildren = getChildrenCount(); i < numChildren; i++) {
                PNode child = getChild(i);
                if (child instanceof Item) {
                    Item it = (Item)child;
                    it.terminateAnimationIfStepping();
                    it.animateToTransform(item.shift(it.getTransform()), collapseAnimationDuration);
                }
            }

            item.setCollapsed(!item.isCollapsed());
//            item.getBody().setVisible(!item.isCollapsed());
        }
    }

    public Item findItemByLabel(PNode label) {
        for (Item item : PiccoloUtils.childrenOfType(this, Item.class)) {
            if (item.getLabel() == label) return item;
        }
        return null;
    }

    public class Item extends PNode {
        private final PNode label;
        private final PNode head;
        private final PNode body;

        private boolean collapsed;
        private PActivity lastActivity;

        private Item(PNode label, PNode head, PNode body, boolean collapsed) {
            this.label = label;
            this.head = head;
            this.body = body;
            this.collapsed = collapsed;
            if (label != null) {
                addChild(label);
            }
            if (head != null) {
                addChild(head);
            }
            if (body != null) {
                addChild(body);
            }
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

        public boolean isCollapsed() {
            return collapsed;
        }

        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
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
            PBounds b = new PBounds();
            for (PNode child : PiccoloUtils.childrenOf(body)) {
                b.add(child.getFullBoundsReference());
            }
            return b;
        }

    }


}
