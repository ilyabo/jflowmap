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

package jflowmap.visuals.timeline;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import jflowmap.FlowMapAttrsSpec;
import prefuse.data.Node;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * @author Ilya Boyandin
 */
public class VisualTimelineNodeCell extends PNode {

    private final Node graphNode;
    private final VisualFlowTimeline timeline;

    public VisualTimelineNodeCell(VisualFlowTimeline timeline, Node graphNode, double x, double y, double width, double height) {
        this.graphNode = graphNode;
        this.timeline = timeline;

        FlowMapAttrsSpec specs = timeline.getAttrSpecs();

        setBounds(x, y, width, height);

        PPath rect = new PPath(new Rectangle2D.Double(x, y, width, height));
        rect.setStrokePaint(Color.lightGray);
        rect.setPaint(Color.white);
        addChild(rect);

        String label = graphNode.getString(specs.getNodeLabelAttr());
        PText text = new PText(label);
        text.setX(x + 2);
        text.setY(y + 2);
        addChild(text);
    }

}
