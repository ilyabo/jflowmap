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
package jflowmap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import jflowmap.data.FlowMapStats;
import jflowmap.models.FlowMapModel;
import jflowmap.visuals.VisualFlowMap;
import jflowmap.visuals.VisualNode;

import org.apache.log4j.Logger;

import prefuse.data.io.DataIOException;
import at.fhj.utils.misc.FileUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class SmallMultiplesMain {


    private static Logger logger = Logger.getLogger(SmallMultiplesMain.class);

    private static final Font TITLE_FONT = new Font("Dialog", Font.BOLD, 32);
    private static final Font LABEL_FONT = new Font("Dialog", Font.PLAIN, 5);
    private static final Color LABEL_COLOR = Color.gray;
    private static final Color BACKGROUND_COLOR = new Color(0x60, 0x60, 0x60);

    private static final int FRAME_WIDTH = 1280;
    private static final int FRAME_HEIGHT = 1024;

    private static void setupFlowMapModel(FlowMapModel model) {
        model.setShowNodes(true);
        model.setMaxEdgeWidth(15);
        model.setNodeSize(3);
        model.setShowDirectionMarkers(true);
        model.setDirectionMarkerSize(.17);
        model.setDirectionMarkerAlpha(255);
        model.setEdgeAlpha(100);
    }

    static class RenderTask extends SwingWorker<Void, Void> {

        private static final boolean USE_GLOBAL_VISUAL_MAPPINGS = true;

        private static final double ZOOM_LEVEL = 1.3;
        private static final double MOVE_DX = 30;
        private static final double MOVE_DY = -50;

        final Map<String, DatasetSpec> datasets;

        final DatasetSpec datasetSpec = new DatasetSpec(
                "data/refugees/refugees-{name}.xml", "ritypnv", "x", "y", "name", "data/refugees/countries-areas.xml"
        );
        final String outputFileName = "refugees-small-multiples.png";

//        final List<String> datasetNames = Arrays.asList("1994", "1996", "2000", "2007", "2008");
//        final List<String> datasetNames = Arrays.asList("1996", "2000", "2008");
        final List<String> datasetNames = Arrays.asList("1994", "2000", "2007");

//        final List<String> datasetNames;
//        final int startYear = 2001;
//        final int endYear = 2008;
//        final int yearStep = +1;
//        final int n = ((endYear - startYear) / yearStep) + 1;
//        {
//            datasetNames = Lists.newArrayList();
//            for (int i = 0; i < n; i++) {
//                datasetNames.add(Integer.toString(startYear + i * yearStep));
//            }
//        }


        final int numColumns = 3;
        final int paddingX = 5;
        final int paddingY = 5;

        private final ProgressMonitor progress;
        private final JFlowMap jFlowMap;
        private final int width;
        private final int height;
        private final int totalWidth;
        private final int totalHeight;
        private final BufferedImage image;
        private final JFrame parentFrame;

        public RenderTask(JFrame parent, JFlowMap jFlowMap) {
            this.jFlowMap = jFlowMap;
            this.parentFrame = parent;

            datasets = Maps.newLinkedHashMap();
            for (String name : datasetNames) {
                datasets.put(name, datasetSpec.withFilename(datasetSpec.getFilename().replace("{name}", name)));
            }

            final int n = datasets.size();
            progress = new ProgressMonitor(parent, "Rendering small multiples", "", 0, n);

            width = jFlowMap.getWidth();
            height = jFlowMap.getHeight();
            totalWidth = Math.min(n, numColumns) * (width + paddingX) + paddingX;
            totalHeight = ((int)Math.ceil((double)n / numColumns)) * (height + paddingY) + paddingY;

            image = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);

        }

        @Override
        public Void doInBackground() {
            try {
                renderFlowMap();
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
            return null;
        }

        private void renderFlowMap() throws InterruptedException, InvocationTargetException, DataIOException {

            final FlowMapStats stats;
            if (USE_GLOBAL_VISUAL_MAPPINGS) {
                // calc the global stats
                List<FlowMapGraphWithAttrSpecs> gs = Lists.newArrayList();
                for (Map.Entry<String, DatasetSpec> entry : datasets.entrySet()) {
                    final String name = entry.getKey();
                    final DatasetSpec ds = entry.getValue();
                    progress.setNote("Gathering stats for " + name);
                    gs.add(new FlowMapGraphWithAttrSpecs(JFlowMap.loadGraph(ds.getFilename()), ds.getAttrsSpec()));
                }
                stats = FlowMapStats.createFor(gs);
            } else {
                stats = null;
            }


            final Graphics2D g = (Graphics2D)image.getGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, totalWidth, totalHeight);


            int cycle = 0;
            for (Map.Entry<String, DatasetSpec> entry : datasets.entrySet()) {
                final String name = entry.getKey();
                final DatasetSpec ds = entry.getValue();

                if (progress.isCanceled()) {
                    break;
                }
//                final String name = Integer.toString(startYear + i * yearStep);
//                final DatasetSpec ds = datasetSpec.withFilename(filenameTemplate.replace("{year}", name));
                final int _cycle = cycle;

                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        parentFrame.setTitle(name);
                        jFlowMap.loadFlowMap(ds, stats);

                        FlowMapModel model = jFlowMap.getVisualFlowMap().getModel();
                        setupFlowMapModel(model);

                    }
                });
                if (progress.isCanceled()) {
                    break;
                }
                final VisualFlowMap visualFlowMap = jFlowMap.getVisualFlowMap();
                if (_cycle == 0) {
                    // Run only the first time
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            visualFlowMap.fitInCameraView();
                        }
                    });
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            PCamera camera = visualFlowMap.getCamera();
                            PBounds viewBounds = camera.getViewBounds();
                            camera.scaleViewAboutPoint(ZOOM_LEVEL, viewBounds.x + viewBounds.width / 2, viewBounds.y + viewBounds.height / 2);
                            viewBounds = (PBounds) camera.getViewBounds().clone();
                            viewBounds.x += MOVE_DX;
                            viewBounds.y += MOVE_DY;
                            camera.setViewBounds(viewBounds);
                        }
                    });
                }
                if (progress.isCanceled()) {
                    break;
                }
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        visualFlowMap.addChild(createTitleNode(name, visualFlowMap.getCamera().getViewBounds()));
                        visualFlowMap.addChild(createLabelsNode(name, visualFlowMap));

                        // Pain the plot
                        final int x = paddingX + (width + paddingX) * (_cycle % numColumns);
                        final int y = paddingY + (height + paddingY) * (_cycle / numColumns);

                        g.translate(x, y);

                        jFlowMap.paint(g);

                        g.setColor(LABEL_COLOR);
                        g.setFont(LABEL_FONT);

                        g.translate(-x, -y);

                        progress.setProgress(_cycle);
                        progress.setNote("Rendering graphic " + (_cycle + 1) + " of " + datasets.size());
                    }
                });

                cycle++;
            }
        }

        @Override
        public void done() {
            if (!progress.isCanceled()) {
                progress.setNote("Writing image to file " + outputFileName);
                try {
                    ImageIO.write(image, FileUtils.getExtension(outputFileName), new File(outputFileName));
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(parentFrame,  "Couldn't save image [" + e.getClass().getSimpleName()+ "] " + e.getMessage());
                    logger.error(e);
                }
            }
            System.exit(0);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {

        final JFrame frame = new JFrame();
        final JFlowMap jFlowMap = new JFlowMap(null, false);
        frame.add(jFlowMap);

        Dimension size = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setSize(size);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        jFlowMap.fitFlowMapInView();

        RenderTask task = new RenderTask(frame, jFlowMap);
//        task.addPropertyChangeListener(this);
        task.execute();
    }


    private static PNode createTitleNode(final String title, PBounds cameraBounds) {
        PText ptext = new PText(title);
        ptext.setX(cameraBounds.getX());
        ptext.setY(cameraBounds.getY() + cameraBounds.getHeight() - TITLE_FONT.getSize2D());
        ptext.setFont(TITLE_FONT);
        ptext.setTextPaint(LABEL_COLOR);
        return ptext;
    }

    private static PNode createLabelsNode(String year, VisualFlowMap visualFlowMap) {
        PNode labelLayer = new PNode();
        addLabelTextNode("Stateless", visualFlowMap, labelLayer);
        addLabelTextNode("Various", visualFlowMap, labelLayer);
        return labelLayer;
    }


    private static PText addLabelTextNode(String label, VisualFlowMap visualFlowMap, PNode labelLayer) {
        VisualNode node = visualFlowMap.getVisualNodeByLabel(label);
        PText ptext = new PText(node.getLabel());
        ptext.setFont(LABEL_FONT);
        ptext.setTextPaint(LABEL_COLOR);
        double width = 20;
        double height = LABEL_FONT.getSize2D();
        ptext.setBounds(node.getX() - width/2, node.getY() + visualFlowMap.getModel().getNodeSize() * 1.1, width, height);
        ptext.setJustification(JLabel.CENTER_ALIGNMENT);
        labelLayer.addChild(ptext);
        return ptext;
    }
}
