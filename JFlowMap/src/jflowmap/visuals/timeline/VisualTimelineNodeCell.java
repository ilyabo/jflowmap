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
import jflowmap.JFlowTimeline;
import jflowmap.data.FlowMapStats;
import jflowmap.data.MinMax;
import jflowmap.util.ArrayUtils;
import jflowmap.util.ColorUtils;
import jflowmap.util.PiccoloUtils;
import prefuse.data.Node;
import prefuse.util.ColorLib;
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

    private static final Color VALUE_COLOR_MIN = new Color(255, 245, 240);
    private static final Color VALUE_COLOR_MAX = new Color(103, 0, 13);
    private static final Color VALUE_COLOR_NAN = JFlowTimeline.CANVAS_BACKGROUND_COLOR; // new Color(200, 200, 200);

    public static final int[] DIFF_COLORS = ArrayUtils.reverse(new int[] {
        ColorLib.rgb(165, 0, 38), ColorLib.rgb(215, 48, 39), ColorLib.rgb(244, 109, 67), ColorLib.rgb(253, 174, 97), ColorLib.rgb(254, 224, 139),
        ColorLib.rgb(255, 255, 191),
        ColorLib.rgb(217, 239, 139), ColorLib.rgb(166, 217, 106), ColorLib.rgb(102, 189, 99), ColorLib.rgb(26, 152, 80), ColorLib.rgb(0, 104, 55),
    });
    private static final Color DIFF_COLOR_MIN = new Color(26, 152, 80);
    private static final Color DIFF_COLOR_ZERO = new Color(255, 255, 191);
    private static final Color DIFF_COLOR_MAX = new Color(215, 48, 39);
    private static final Color DIFF_COLOR_NAN = JFlowTimeline.CANVAS_BACKGROUND_COLOR;

    private static final double DIFF_BOX_WIDTH = 10;
    private static final double DIFF_BOX_GAP = 0;

    private static final Color NODE_NAME_COLOR1 = new Color(0, 0, 0);
    private static final Color NODE_NAME_COLOR2 = new Color(255, 255, 255);

    private static final Color SELECTION_STROKE_PAINT = new Color(200, 100, 30);
    private static final PFixedWidthStroke SELECTION_STROKE = new PFixedWidthStroke(4);
    private static final Font CELL_CAPTION_FONT = new Font("Arial", Font.PLAIN, 18);
    private static final Font CELL_VALUE_FONT = new Font("Arial", Font.PLAIN, 10);
    private final Node graphNode;
    private final VisualFlowTimeline timeline;
    private final PPath rect;
    private final PPath diffRect;

    public VisualTimelineNodeCell(VisualFlowTimeline timeline, Node graphNode, double x, double y, double width, double height) {
        this.graphNode = graphNode;
        this.timeline = timeline;

        FlowMapAttrsSpec specs = timeline.getAttrSpecs();

        setBounds(x, y, width, height);

        rect = new PPath(new Rectangle2D.Double(x + DIFF_BOX_WIDTH + DIFF_BOX_GAP, y, width - DIFF_BOX_WIDTH - DIFF_BOX_GAP, height));
//        rect.setStrokePaint(Color.white);
        rect.setStroke(null);
//        rectNode.setPaint(Color.white);
        addChild(rect);




        PText nodeNameText = new PText(graphNode.getString(specs.getNodeLabelAttr()));
        nodeNameText.setFont(CELL_CAPTION_FONT);
//        text.setBounds(rect.getBounds());
//        text.setJustification(JComponent.CENTER_ALIGNMENT);
        nodeNameText.setX(rect.getX() + 3);
        nodeNameText.setY(rect.getY() + 2);
        addChild(nodeNameText);

        double value = graphNode.getDouble(FlowMapStats.NODE_STATS_COLUMN__SUM_OUTGOING);

        PText valueText = new PText(JFlowMap.NUMBER_FORMAT.format(value));
        valueText.setFont(CELL_VALUE_FONT);
        valueText.setJustification(JComponent.RIGHT_ALIGNMENT);
        valueText.setTextPaint(Color.gray);
        valueText.setBounds(
                rect.getX() - valueText.getWidth() + rect.getWidth(), rect.getY() - valueText.getHeight() + rect.getHeight(),
                valueText.getWidth(), valueText.getHeight());
        addChild(valueText);


        Color rectColor;
        MinMax vstats = timeline.getGlobalStats().getNodeAttrStats(FlowMapStats.NODE_STATS_COLUMN__SUM_OUTGOING);
        if (!Double.isNaN(value)) {
            double normalizedValue =
                vstats.normalizeLog(value);
//                vstats.normalize(value);
            rectColor = ColorUtils.colorBetween(
                    VALUE_COLOR_MIN,
                    VALUE_COLOR_MAX,
                    normalizedValue, 255
            );
        } else {
            rectColor = VALUE_COLOR_NAN;
        }
        rect.setPaint(rectColor);
        Color textColor = ColorUtils.farthestColor(NODE_NAME_COLOR1, NODE_NAME_COLOR2, rectColor);
        nodeNameText.setTextPaint(textColor);
        valueText.setTextPaint(textColor);


        diffRect = new PPath(new Rectangle2D.Double(x, y, DIFF_BOX_WIDTH, height));
        diffRect.setPaint(new Color(0, 68, 27));
        diffRect.setStroke(null);
        addChild(diffRect);

        Color diffRectColor;
        double diff = graphNode.getDouble(FlowMapStats.NODE_STATS_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR);

        MinMax diffStats = timeline.getGlobalStats().getNodeAttrStats(
                FlowMapStats.NODE_STATS_COLUMN__SUM_OUTGOING_DIFF_TO_NEXT_YEAR);
        if (!Double.isNaN(diff)) {
            // TODO: Use ColorMap instead
//            double normalizedDiff =
////                diffStats.normalizeLog(diff);
//                diffStats.normalize(diff);
//            diffRectColor = ColorUtils.colorBetween(
//                    DIFF_COLOR_MIN,
//                    DIFF_COLOR_MAX,
//                    normalizedDiff, 255
//            );
            diffRectColor = ColorLib.getColor(timeline.getSumOutgoingDiffColorMap().getColor(diff));
        } else {
            diffRectColor = DIFF_COLOR_NAN;
        }
        diffRect.setPaint(diffRectColor);




//

        addInputEventListener(inputEventListener);
    }




    private static final PInputEventListener inputEventListener = new PBasicInputEventHandler() {
        @Override
        public void mouseEntered(PInputEvent event) {
            VisualTimelineNodeCell vc = PiccoloUtils.getParentNodeOfType(event.getPickedNode(), VisualTimelineNodeCell.class);
            if (vc != null) {
                vc.rect.setStroke(SELECTION_STROKE);
                vc.rect.setStrokePaint(SELECTION_STROKE_PAINT);
            }
        }

        @Override
        public void mouseExited(PInputEvent event) {
            VisualTimelineNodeCell vc = PiccoloUtils.getParentNodeOfType(event.getPickedNode(), VisualTimelineNodeCell.class);
            if (vc != null) {
                vc.rect.setStroke(null);
                vc.rect.setStrokePaint(null);
            }
        }
    };

}
