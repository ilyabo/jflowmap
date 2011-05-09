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

package jflowmap.util.piccolo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.RoundRectangle2D;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * To attach a listener use:
 * <pre>
 *  button.addInputEventListener(new PBasicInputEventHandler() {
 *     public void mouseClicked(PInputEvent event) {
 *
 *     }
 *   });
 * </pre>
 *
 * @author Ilya Boyandin
 */
public class PButton extends PPath {

  private static Font FONT = new Font("Dialog", Font.PLAIN, 10);

  private static final BasicStroke DEFAULT_STROKE = new BasicStroke(.5f);
  private static final Color DEFAULT_PAINT = Color.white;
  private static final Color DEFAULT_STROKE_PAINT = new Color(120, 120, 120);
  private static final Color DEFAULT_TEXT_PAINT = DEFAULT_STROKE_PAINT;

  private static final Color ARMED_PAINT = new Color(150, 150, 150);
  private static final Color ARMED_STROKE_PAINT = DEFAULT_STROKE_PAINT;
  private static final Color ARMED_TEXT_PAINT = Color.white;

  private static final double ARCW = 10;
  private static final double ARCH = 20;

  private static final int PADDING_X = 5;
  private static final int PADDING_Y = 5;

  private final PText textNode;
  private boolean isArmed = false;

  public PButton(String text) {
    super(new RoundRectangle2D.Double(0, 0, 100, 100, ARCW, ARCH));

    textNode = new PText(text.toUpperCase());
    textNode.setFont(FONT);
    addChild(textNode);

    updateBounds();

    setStroke(DEFAULT_STROKE);
    updateColors();

    addInputEventListener(createMouseHandler());
  }

  private void setArmed(boolean armed) {
    if (this.isArmed != armed) {
      this.isArmed = armed;
      updateColors();
    }
  }

  public boolean setPosition(double x, double y) {
    return setBounds(x, y, getWidth(), getHeight());
  }

  private void updateColors() {
    if (isArmed) {
      setPaint(ARMED_PAINT);
      setStrokePaint(ARMED_STROKE_PAINT);
      textNode.setPaint(ARMED_PAINT);
      textNode.setTextPaint(ARMED_TEXT_PAINT);
    } else {
      setPaint(DEFAULT_PAINT);
      setStrokePaint(DEFAULT_STROKE_PAINT);
      textNode.setPaint(DEFAULT_PAINT);
      textNode.setTextPaint(DEFAULT_TEXT_PAINT);
    }
  }

  private PBasicInputEventHandler createMouseHandler() {
    return new PBasicInputEventHandler() {
      @Override
      public void mouseEntered(PInputEvent event) {
        setArmed(true);
      }

      @Override
      public void mouseExited(PInputEvent event) {
        setArmed(false);
      }
    };
  }

  private void updateBounds() {
    if (textNode != null) {
      PBounds tb = textNode.getBoundsReference();
      setBounds(getX(), getY(), tb.width + PADDING_X * 2, tb.height + PADDING_Y * 2);
      textNode.setBounds(getX() + PADDING_X, getY() + PADDING_Y, tb.width, tb.height);
    }
  }

  @Override
  public boolean setBounds(double x, double y, double width, double height) {
    if (super.setBounds(x, y, width, height)) {
      updateBounds();
      return true;
    }
    return false;
  }

}
