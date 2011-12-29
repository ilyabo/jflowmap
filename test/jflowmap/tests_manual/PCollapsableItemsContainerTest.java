package jflowmap.tests_manual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Random;

import jflowmap.util.piccolo.PCollapsableItemsContainer;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
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

//    PNode body;
//    if (rnd.nextInt() % 2 == 0) {
//      PCollapsableItemsContainer pa = new PCollapsableItemsContainer();
//      pa
//      pa.layoutItems();
//      body = pa;
//    } else {
//      body = createRandomRect(Color.red);
//    }

    acc = createAccordion(0);
    getCanvas().getLayer().addChild(acc);
    acc.setX(100);
    acc.setY(50);
    acc.layoutItems();
  }

  private PCollapsableItemsContainer createAccordion(int level) {
    PCollapsableItemsContainer acc = new PCollapsableItemsContainer();
    for (int i = 0; i < 5; i++) {
      PNode body;
      if (level == 0  &&  rnd.nextInt(2) == 0) {
        PCollapsableItemsContainer subacc = createAccordion(level + 1);
        body = subacc;
        subacc.layoutItems();
      } else {
        body = createRandomRect(Color.red);
      }

      acc.addNewItem("l" + (level + 1) + " item " + i, createRandomRect(Color.blue), body);
    }
    return acc;
  }

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
