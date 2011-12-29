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

package jflowmap.geom;

import java.awt.geom.Path2D;


/**
 * @author Ilya Boyandin
 */
public class ArrowQuadPath extends Path2D.Double {

  private static final long serialVersionUID = 7247894703636210080L;

  /**
   * @param cx, cy Control point of the curve
   */
  public ArrowQuadPath(double x1, double y1, double x2, double y2, double cx, double cy, double arrSize) {
    double adjSize = (arrSize/Math.sqrt(2));
    double ex = x2 - cx;
    double ey = y2 - cy;
    double abs_e = Math.sqrt(ex*ex + ey*ey);
    ex /= abs_e;
    ey /= abs_e;


    // Creating quad arrow
    moveTo(x1, y1);
    quadTo(cx, cy, x2, y2);
    lineTo(x2 + (ey-ex)*adjSize, y2 - (ex + ey)*adjSize);
    moveTo(x2, y2);
    lineTo(x2 - (ey + ex)*adjSize, y2 + (ex - ey)*adjSize);
  }

}
