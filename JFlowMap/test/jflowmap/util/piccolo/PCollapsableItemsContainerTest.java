package jflowmap.util.piccolo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Random;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.PFrame;

/**
 * @author Ilya Boyandin
 */
public class PCollapsableItemsContainerTest extends PFrame {
    private static final BasicStroke A_STROKE = new BasicStroke(2);
    private final Random rnd = new Random();
    final PCollapsableItemsContainer acc;

    public PCollapsableItemsContainerTest() {
        super("PCollapsableItemsContainerTest", false, null);
        setSize(640, 800);

        getCanvas().removeInputEventListener(getCanvas().getPanEventHandler());

//        PNode body;
//        if (rnd.nextInt() % 2 == 0) {
//            PCollapsableItemsContainer pa = new PCollapsableItemsContainer();
//            pa
//            pa.layoutItems();
//            body = pa;
//        } else {
//            body = createRandomRect(Color.red);
//        }

        acc = createAccordion(0);
        acc.setX(100);
        acc.setY(50);
        acc.layoutItems();
        getCanvas().getLayer().addChild(acc);
    }

    private PCollapsableItemsContainer createAccordion(int level) {
        PCollapsableItemsContainer acc = new PCollapsableItemsContainer();
        CollapseHandler ch = new CollapseHandler(acc);
        for (int i = 0; i < 5; i++) {
            PLabel label = new PLabel("l" + (level + 1) + " item " + i);
            label.addInputEventListener(ch);
            PNode body;
            if (level == 0  &&  rnd.nextInt(2) == 0) {
                PCollapsableItemsContainer subacc = createAccordion(level + 1);
                body = subacc;
                subacc.layoutItems();
            } else {
                body = createRandomRect(Color.red);
            }

            PCollapsableItemsContainer.Item item = acc.addNewItem(label, createRandomRect(Color.blue), body);
            label.setItem(item);
        }
        return acc;
    }

    private static final Color NON_SEL_LABEL_BG = new Color(69, 117, 180);
    private static final Color SEL_LABEL_BG = new Color(215, 48, 39);
    private static final Color PRESSED_SEL_LABEL_BG = new Color(225, 58, 49);

    private static final Color NON_SEL_LABEL_FG = Color.white;
    private static final Color SEL_LABEL_FG = Color.white;

    static class PLabel extends PNode {
        private PCollapsableItemsContainer.Item item;
        private final PText textNode;
        private final PPath rectNode;
        public PLabel(String text) {
            this.textNode = new PText(text);
            final int pad = 5;
            this.rectNode = PPath.createRoundRectangle(-pad, -pad, (float)textNode.getWidth() + 2*pad, (float)textNode.getHeight() + 2*pad, 5, 5);

            rectNode.setPaint(NON_SEL_LABEL_BG);
            rectNode.setStroke(null);
            addChild(rectNode);

            textNode.setTextPaint(NON_SEL_LABEL_FG);
            addChild(textNode);
        }
        public PCollapsableItemsContainer.Item getItem() {
            return item;
        }
        public void setItem(PCollapsableItemsContainer.Item item) {
            this.item = item;
        }
        public PText getTextNode() {
            return textNode;
        }
        public PPath getRectNode() {
            return rectNode;
        }
    }


    static class CollapseHandler extends PBasicInputEventHandler {
        PCollapsableItemsContainer acc;

        private CollapseHandler(PCollapsableItemsContainer acc) {
            this.acc = acc;
        }

        @Override
        public void mouseEntered(PInputEvent event) {
            PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
            if (label != null) {
                label.getTextNode().setTextPaint(SEL_LABEL_FG);
                label.getRectNode().setPaint(SEL_LABEL_BG);
            }
        }
        @Override
        public void mouseExited(PInputEvent event) {
            PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
            if (label != null) {
                label.getTextNode().setTextPaint(NON_SEL_LABEL_FG);
                label.getRectNode().setPaint(NON_SEL_LABEL_BG);
            }
        }
        @Override
        public void mousePressed(PInputEvent event) {
            PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
            if (label != null) {
                label.getRectNode().setPaint(PRESSED_SEL_LABEL_BG);
            }
        }
        @Override
        public void mouseReleased(PInputEvent event) {
            PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
            if (label != null) {
                label.getRectNode().setPaint(SEL_LABEL_BG);
            }
        }
        @Override
        public void mouseClicked(PInputEvent event) {
            PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
            if (label != null) {
                acc.toggleCollapsed(acc.findItemByLabel(label));
            }
        }
    };

    private PPath createRandomRect(Color color) {
        PPath rect = PPath.createRectangle(rnd.nextInt(100), rnd.nextInt(100), rnd.nextInt(100) + 100, rnd.nextInt(50) + 50);
        rect.setStrokePaint(color);
        rect.setStroke(A_STROKE);
        PBounds b = rect.getBounds();
        final int N = rnd.nextInt(6) + 1;
        for (int i = 0; i < N; i++) {
            PPath r = PPath.createRectangle((float)(b.x + i * (b.width-10) / N) + 5, (float)b.y + 5, (float)(b.width-20) / N, (float)b.height - 10);
            r.setStrokePaint(Color.green);
            r.setStroke(A_STROKE);
            rect.addChild(r);
        }
        return rect;
    }

    public static void main(String[] args) {
        new PCollapsableItemsContainerTest();
    }

}
