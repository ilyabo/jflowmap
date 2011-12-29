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
import java.awt.Component;
import java.awt.Font;
import java.awt.Shape;
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

  private static final Color PRESSED_PAINT = new Color(130, 130, 130);
  private static final Color PRESSED_STROKE_PAINT = DEFAULT_STROKE_PAINT;
  private static final Color PRESSED_TEXT_PAINT = Color.white;

  private static final Color ROLLOVER_PAINT = new Color(220, 220, 220);
  private static final Color ROLLOVER_STROKE_PAINT = new Color(150, 150, 150);
  private static final Color ROLLOVER_TEXT_PAINT = new Color(90, 90, 90);

  private static final Color ROLLOVER_PRESSED_PAINT = new Color(140, 140, 140);
  private static final Color ROLLOVER_PRESSED_STROKE_PAINT = new Color(130, 130, 130);
  private static final Color ROLLOVER_PRESSED_TEXT_PAINT = PRESSED_TEXT_PAINT;

  private static final Color ARMED_PAINT = new Color(170, 170, 170);
  private static final Color ARMED_STROKE_PAINT =  new Color(150, 150, 150);
  private static final Color ARMED_TEXT_PAINT = Color.white;

  private static final double ARCW = 20;
  private static final double ARCH = 40;

  private static final int PADDING_X = 5;
  private static final int PADDING_Y = 5;

  private final PText textNode;
  private boolean isArmed = false;
  private boolean isRollover = false;
  private boolean isPressed = false;

  private final boolean isToggleButton;

  public PButton(String text) {
    this(text, false);
  }

  public PButton(String text, boolean toggle) {
    this(text, toggle, FONT);
  }

  public PButton(String text, boolean toggle, Font font) {
    super(createShape());
    this.isToggleButton = toggle;

    textNode = new PText(text.toUpperCase());
    textNode.setFont(font);
    textNode.setHorizontalAlignment(Component.CENTER_ALIGNMENT);
    addChild(textNode);

    updateBounds();

    setStroke(DEFAULT_STROKE);
    updateColors();

    addInputEventListener(createMouseHandler());
  }

  private static Shape createShape() {
    return new RoundRectangle2D.Double(0, 0, 100, 100, ARCW, ARCH);
  }

  private void setArmed(boolean armed) {
    if (this.isArmed != armed) {
      this.isArmed = armed;
      updateColors();
    }
  }

  public boolean isArmed() {
    return isArmed;
  }

  public void setRollover(boolean rollover) {
    if (this.isRollover != rollover) {
      this.isRollover = rollover;
      updateColors();
    }
  }

  public boolean isRollover() {
    return isRollover;
  }

  public boolean setPosition(double x, double y) {
    return setBounds(x, y, getWidth(), getHeight());
  }

  public void setPressed(boolean isPressed) {
    if (this.isPressed != isPressed) {
      this.isPressed = isPressed;
      updateColors();
    }
  }

  public boolean isPressed() {
    return isPressed;
  }

  private void updateColors() {
    if (isArmed) {
      setPaint(ARMED_PAINT);
      setStrokePaint(ARMED_STROKE_PAINT);
      textNode.setTextPaint(ARMED_TEXT_PAINT);
    } else if (isRollover  &&  !isPressed) {
      setPaint(ROLLOVER_PAINT);
      setStrokePaint(ROLLOVER_STROKE_PAINT);
      textNode.setTextPaint(ROLLOVER_TEXT_PAINT);
    } else if (isRollover  &&  isPressed) {
      setPaint(ROLLOVER_PRESSED_PAINT);
      setStrokePaint(ROLLOVER_PRESSED_STROKE_PAINT);
      textNode.setTextPaint(ROLLOVER_PRESSED_TEXT_PAINT);
    } else if (isPressed) {
      setPaint(PRESSED_PAINT);
      setStrokePaint(PRESSED_STROKE_PAINT);
      textNode.setTextPaint(PRESSED_TEXT_PAINT);
    } else {
      setPaint(DEFAULT_PAINT);
      setStrokePaint(DEFAULT_STROKE_PAINT);
      textNode.setTextPaint(DEFAULT_TEXT_PAINT);
    }
  }

  private PBasicInputEventHandler createMouseHandler() {
    return new PBasicInputEventHandler() {
      @Override
      public void mouseEntered(PInputEvent event) {
        setRollover(true);
      }

      @Override
      public void mouseExited(PInputEvent event) {
        setRollover(false);
      }

      @Override
      public void mousePressed(PInputEvent event) {
        setArmed(true);
      }

      @Override
      public void mouseReleased(PInputEvent event) {
        if (isToggleButton  &&  isRollover) {
          setPressed(!isPressed);
        }
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
