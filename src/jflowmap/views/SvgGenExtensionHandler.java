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
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import jflowmap.util.ColorUtils;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.apache.batik.svggen.SVGSyntax;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Batik svg generator does not support LinearGradientPaint by default, so we have to implement it.
 *
 * @author Ilya Boyandin
 */
class SvgGenExtensionHandler extends DefaultExtensionHandler {

  @Override
  public SVGPaintDescriptor handlePaint(Paint paint, SVGGeneratorContext ctx) {
    if (paint instanceof LinearGradientPaint) {
      LinearGradientPaint gradient = (LinearGradientPaint) paint;

      // Create a new SVG 'linearGradient' element to represent the
      // LinearGradientPaint being used.
      String id = ctx.getIDGenerator().generateID("gradient");
      Document doc = ctx.getDOMFactory();
      Element grad = doc.createElementNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_LINEAR_GRADIENT_TAG);

      addAttrs(gradient, id, grad);
      addTransformAttr(gradient, grad);
      addStopTags(gradient, doc, grad);

      return new SVGPaintDescriptor("url(#" + id + ")", SVGSyntax.SVG_OPAQUE_VALUE, grad);
    } else {
      return null;
    }
  }

  public void addAttrs(LinearGradientPaint gradient, String id, Element grad) {
    grad.setAttributeNS(null, SVGSyntax.SVG_ID_ATTRIBUTE, id);
    grad.setAttributeNS(null, SVGSyntax.SVG_GRADIENT_UNITS_ATTRIBUTE, SVGSyntax.SVG_USER_SPACE_ON_USE_VALUE);
    Point2D pt = gradient.getStartPoint();
    grad.setAttributeNS(null, "x1", Double.toString(pt.getX()));
    grad.setAttributeNS(null, "y1", Double.toString(pt.getY()));
    pt = gradient.getEndPoint();
    grad.setAttributeNS(null, "x2", Double.toString(pt.getX()));
    grad.setAttributeNS(null, "y2", Double.toString(pt.getY()));

    switch (gradient.getCycleMethod()) {
    case REFLECT:
      grad.setAttributeNS(null, SVGSyntax.SVG_SPREAD_METHOD_ATTRIBUTE, SVGSyntax.SVG_REFLECT_VALUE);
      break;
    case REPEAT:
      grad.setAttributeNS(null, SVGSyntax.SVG_SPREAD_METHOD_ATTRIBUTE, SVGSyntax.SVG_REPEAT_VALUE);
      break;
    // 'pad' is the default...
    }
  }

  public void addStopTags(LinearGradientPaint gradient, Document doc, Element grad) {
    Color[] colors = gradient.getColors();
    float[] fractions = gradient.getFractions();
    for (int i = 0, length = colors.length; i < length; i++) {
      Color color = colors[i];
      float fraction = fractions[i];
      Element stop = doc.createElementNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_STOP_TAG);
      stop.setAttributeNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_STOP_COLOR_ATTRIBUTE,
          "#" + ColorUtils.toHexString(color));
      stop.setAttributeNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_STOP_OPACITY_ATTRIBUTE,
          Double.toString(color.getAlpha() / 255.0));
      stop.setAttributeNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_OFFSET_ATTRIBUTE,
          Double.toString(fraction * 100) + "%");
      grad.appendChild(stop);
    }
  }

  public void addTransformAttr(LinearGradientPaint gradient, Element grad) {
    AffineTransform transform = gradient.getTransform();
    if (transform != null) {
      double scaleX = transform.getScaleX(), scaleY = transform.getScaleY();
      double translateX = transform.getTranslateX(), translateY = transform.getTranslateY();

      StringBuilder sb = new StringBuilder();
      if (scaleX != 1.0 || scaleY != 1.0) {
        sb.append("scale(").append(scaleX).append(",").append(scaleY).append(")");
      }

      if (translateX != 0.0 || translateY != 0.0) {
        if (sb.length() > 0) {
          sb.append(",");
        }
        sb.append("translate(").append(scaleX).append(",").append(scaleY).append(")");
      }

      grad.setAttributeNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_TRANSFORM_ATTRIBUTE, sb.toString());
    }
  }

}