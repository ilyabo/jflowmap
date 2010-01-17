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

package jflowmap.visuals;

import java.awt.Stroke;

import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class VisualEdgeStrokeFactory {

    private final VisualFlowMap visualFlowMap;

    public VisualEdgeStrokeFactory(VisualFlowMap visualFlowMap) {
        this.visualFlowMap = visualFlowMap;
    }

    public Stroke createStroke(double normalizedValue) {
        float width = (float)(1 + normalizedValue * visualFlowMap.getModel().getMaxEdgeWidth());
        return new PFixedWidthStroke(width);
//        return new BasicStroke(width);
    }

}
