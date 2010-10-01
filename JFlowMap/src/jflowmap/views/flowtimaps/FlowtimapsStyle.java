/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.views.flowtimaps;

import java.awt.Color;
import java.awt.Stroke;

import jflowmap.NodeEdgePos;

/**
 * @author Ilya Boyandin
 */
public interface FlowtimapsStyle {

  Color getBackgroundColor();

  Color getFlowCircleColor();

  Color getFlowLineHighlightedColor();

  Color getFlowLineColor();

  Color getMissingValueColor();

//  int[] getSequentialValueColors();
//
//  int[] getDivergingValueColors();

  Stroke getSelectedTimelineCellStroke();

  Color getSelectedTimelineCellStrokeColor();

  Stroke getTimelineCellStroke();

  Color getTimelineCellStrokeColor();

  Color getMapAreaCentroidPaint();

  Color getMapAreaSelectedCentroidPaint();

  Color getLassoStrokePaint(NodeEdgePos s);

  Color getMapAreaHighlightedStrokePaint();

  Color getMapAreaHighlightedPaint();

  Color getMapAreaSelectedCentroidLabelTextPaint();

  Color getMapAreaCentroidLabelTextPaint();

  Color getMapAreaSelectedCentroidLabelPaint();

  Color getMapAreaCentroidLabelPaint();

  Color getMapAreaHasNoFlowsColor();

}
