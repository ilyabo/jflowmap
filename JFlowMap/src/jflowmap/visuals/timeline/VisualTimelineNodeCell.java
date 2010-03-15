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
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import jflowmap.FlowMapAttrsSpec;
import jflowmap.JFlowMap;
import jflowmap.data.FlowMapStats;
import jflowmap.util.PiccoloUtils;
import prefuse.data.Node;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class VisualTimelineNodeCell extends PNode {

    private static final Color SELECTION_STROKE_PAINT = new Color(200, 100, 30);
    private static final PFixedWidthStroke SELECTION_STROKE = new PFixedWidthStroke(4);
    private static final Font CELL_CAPTION_FONT = new Font("Arial", Font.PLAIN, 18);
    private static final Font CELL_VALUE_FONT = new Font("Arial", Font.PLAIN, 10);
    private final Node graphNode;
    private final VisualFlowTimeline timeline;
    private final PPath rectNode;

    public VisualTimelineNodeCell(VisualFlowTimeline timeline, Node graphNode, double x, double y, double width, double height) {
        this.graphNode = graphNode;
        this.timeline = timeline;

        FlowMapAttrsSpec specs = timeline.getAttrSpecs();

        setBounds(x, y, width, height);

        rectNode = new PPath(new Rectangle2D.Double(x, y, width, height));
//        rect.setStrokePaint(Color.darkGray);
        rectNode.setStroke(null);
        rectNode.setPaint(Color.white);
        addChild(rectNode);

        String label = graphNode.getString(specs.getNodeLabelAttr());
        PText caption = new PText(label);
        caption.setFont(CELL_CAPTION_FONT);
//        text.setBounds(rect.getBounds());
//        text.setJustification(JComponent.CENTER_ALIGNMENT);
        caption.setTextPaint(Color.gray);
        caption.setX(x + 3);
        caption.setY(y + 2);
        addChild(caption);

        double sumOutgoing = graphNode.getDouble(FlowMapStats.NODE_STATS_COLUMN__SUM_OUTGOING);
        if (!Double.isNaN(sumOutgoing)) {
            PText v = new PText(JFlowMap.NUMBER_FORMAT.format(sumOutgoing));
            v.setFont(CELL_VALUE_FONT);
            v.setJustification(JComponent.RIGHT_ALIGNMENT);
            v.setTextPaint(Color.gray);
            v.setBounds(
                    x - v.getWidth() + width, y - v.getHeight() + height,
                    v.getWidth(), v.getHeight());
            addChild(v);
        }

//

        addInputEventListener(inputEventListener);
    }


    private static final PInputEventListener inputEventListener = new PBasicInputEventHandler() {
        @Override
        public void mouseEntered(PInputEvent event) {
            VisualTimelineNodeCell vc = PiccoloUtils.getParentNodeOfType(event.getPickedNode(), VisualTimelineNodeCell.class);
            if (vc != null) {
                vc.rectNode.setStroke(SELECTION_STROKE);
                vc.rectNode.setStrokePaint(SELECTION_STROKE_PAINT);
            }
        }

        @Override
        public void mouseExited(PInputEvent event) {
            VisualTimelineNodeCell vc = PiccoloUtils.getParentNodeOfType(event.getPickedNode(), VisualTimelineNodeCell.class);
            if (vc != null) {
                vc.rectNode.setStroke(null);
                vc.rectNode.setStrokePaint(null);
            }
        }
    };

}
