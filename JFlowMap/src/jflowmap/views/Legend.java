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

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import jflowmap.util.piccolo.PBoxLayoutNode;
import jflowmap.util.piccolo.PNodes;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Ilya Boyandin
 */
public class Legend extends PPath {

  private static final long serialVersionUID = -8907313603307434727L;

  static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 12);
  static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 10);
  static final Color TITLE_COLOR = new Color(150, 150, 150);

  private final double gapBetweenLines = 3, gapBeforeText = 7;
  private final double gapAfterHeader = 5;
  private final double gapBetweenTitleAndItemHeader = 2;
  private final double paddingX = 6, paddingY = 4;
  private final double startY = 10;
  private final double startX = 10;

  private final ItemProducer itemsProducer;

  private final Paint textPaint;
  private final String title;

  public Legend(String title, Paint paint, Paint textPaint, ItemProducer itemsProducer) {
    super(new RoundRectangle2D.Double(4, 4, 140, 120, 10, 20));
    this.title = title;
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

  private PText createTitle() {
    PText text = null;
    if (title != null) {
      text = new PText(title);
      text.setFont(TITLE_FONT);
      text.setTextPaint(TITLE_COLOR);
    }
    return text;
  }

  private PNode createHeader() {
    PNode itemsHeader = itemsProducer.createHeader();
    PText title = createTitle();
    if (itemsHeader == null) {
      return title;
    } else if (title == null) {
      return itemsHeader;
    } else {
      PNode box = new PBoxLayoutNode(PBoxLayoutNode.Axis.Y, gapBetweenTitleAndItemHeader);
      box.addChild(title);
      box.addChild(itemsHeader);
      return box;
    }
  }

  public void update() {
    removeAllChildren();

    PNode header = createHeader();

    final double posX = startX + paddingX;
    double posY = startY + paddingY;

    double headerWidth, headerHeight;
    if (header != null) {
      addChild(header);
      PNodes.setPosition(header, posX, posY);
      PBounds headerBounds = header.getFullBoundsReference();
      headerWidth = headerBounds.getWidth();
      headerHeight = headerBounds.getHeight();
      posY += headerHeight + paddingY + gapAfterHeader;
    } else {
      headerWidth = headerHeight = 0;
    }


    double maxItemWidth = Double.NaN;
    double maxLabelX = Double.NaN;
    PText labelNode = null;
    double rightMost = 0;
    int count = 0;

    Iterable<PNode> itemNodes = itemsProducer.createItems();

    List<PText> labelNodes = Lists.newArrayList();
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
      labelNodes.add(labelNode);
      addChild(labelNode);


      double labelHeight = labelNode.getFullBoundsReference().getHeight();
      double height = Math.max(itemHeight, labelHeight) + gapBetweenLines;

      labelNode.setY(item.getY() + (itemHeight - labelHeight)/2);

      double r = labelNode.getX() + labelNode.getWidth();
      if (r > rightMost) {
        rightMost = r;
      }

      posY += height;

      count++;
    }

    if (count > 0) {
      posY -= gapBetweenLines;
      for (PText label : labelNodes) {
        label.setX(maxLabelX);
      }

      for (PNode itemNode : itemNodes) {
        itemNode.setX(posX + (maxItemWidth - itemNode.getWidth())/2);
      }
    }
    setWidth(Math.max(headerWidth, rightMost - getY()) + paddingX * 4);
    setHeight(posY - gapBetweenLines + paddingY * 2);
  }


  private PText createLabel(String itemText, double leftX, double middleY) {
    PText ptext = new PText(itemText);
    ptext.setX(leftX);
    ptext.setY(middleY - Legend.LABEL_FONT.getSize2D()/2 - 1);
    ptext.setFont(Legend.LABEL_FONT);
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
