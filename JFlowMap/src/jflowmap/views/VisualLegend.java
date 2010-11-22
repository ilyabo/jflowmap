/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.views;

import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.RoundRectangle2D;

import jflowmap.util.piccolo.PNodes;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Ilya Boyandin
 */
public class VisualLegend extends PPath {

  private static final long serialVersionUID = -8907313603307434727L;

  static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 13);

  private final double gapBetweenLines = 3, gapBeforeText = 7;
  private final double gapAfterHeader = 5;
  private final double paddingX = 2, paddingY = 2;
  private final double startY = 10;
  private final double startX = 10;

  private final ItemProducer itemsProducer;

  private final Paint textPaint;

  public VisualLegend(Paint paint, Paint textPaint, ItemProducer itemsProducer) {
    super(new RoundRectangle2D.Double(4, 4, 140, 120, 10, 20));
    this.itemsProducer = itemsProducer;
    this.textPaint = textPaint;
    setPaint(paint);
    setStroke(null);
    setStrokePaint(null);
    setPickable(false);
    update();
  }

  public interface ItemProducer {
    PNode createHeader();

    Iterable<PNode> createItems();
  }


  public void update() {
    removeAllChildren();

    PNode header = itemsProducer.createHeader();


    final double posX = startX + paddingX;
    double posY = startY + paddingY;

    PNodes.setPosition(header, posX, posY);
    addChild(header);
    posY += header.getFullBoundsReference().getHeight() + paddingY + gapAfterHeader;


    double maxItemWidth = Double.NaN;
    double maxLabelX = Double.NaN;
    PText labelNode = null;
    double rightMost = 0;
    int count = 0;

    Iterable<PNode> itemNodes = itemsProducer.createItems();

    for (PNode item: itemNodes) {
      addChild(item);
      PNodes.setPosition(item, posX, posY);

      PBounds itemBounds = item.getFullBoundsReference();
      double itemHeight = itemBounds.getHeight();
      double itemWidth = itemBounds.getWidth();
      if (Double.isNaN(maxItemWidth)  ||  maxItemWidth < itemWidth) {
        maxItemWidth = itemWidth;
      }


      double labelX = posX + itemBounds.getWidth() + gapBeforeText;
      if (Double.isNaN(maxLabelX)  ||  labelX > maxLabelX) {
        maxLabelX = labelX;
      }
      labelNode = createLabel(item.getName(), labelX, 0);
      addChild(labelNode);


      double labelHeight = labelNode.getFullBoundsReference().getHeight();
      double height = Math.max(itemHeight, labelHeight);

      labelNode.setY(item.getY() + (itemHeight - labelHeight)/2);

      double r = labelNode.getX() + labelNode.getWidth();
      if (r > rightMost) {
        rightMost = r;
      }

      posY += height + gapBetweenLines;

      count++;
    }

    if (count > 0) {
      for (PText label : PNodes.childrenOfType(this, PText.class)) {
        label.setX(maxLabelX);
      }

      for (PNode itemNode : itemNodes) {
        itemNode.setX(posX + (maxItemWidth - itemNode.getWidth())/2);
      }

      setWidth(rightMost - getY() + paddingY * 2);
      setHeight(posY - gapBetweenLines + paddingX * 2);
    }
  }


  private PText createLabel(String itemText, double leftX, double middleY) {
    PText ptext = new PText(itemText);
    ptext.setX(leftX);
    ptext.setY(middleY - VisualLegend.LABEL_FONT.getSize2D()/2 - 1);
    ptext.setFont(VisualLegend.LABEL_FONT);
    ptext.setTextPaint(textPaint);
//    ptext.setPaint(Color.black);
//    ptext.setTextPaint(Color.white);
//    ptext.setJustification(JLabel.CENTER_ALIGNMENT);
    return ptext;
  }

  @Override
  public void paint(PPaintContext pc) {
    final int oldQuality = pc.getRenderQuality();
    if (oldQuality != PPaintContext.HIGH_QUALITY_RENDERING) {
      pc.setRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
    }

    super.paint(pc);

    if (oldQuality != PPaintContext.HIGH_QUALITY_RENDERING) {
      pc.setRenderQuality(oldQuality);
    }
  }

}
