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

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import jflowmap.models.FlowMapModel;
import at.fhj.utils.misc.FileUtils;

/**
 * @author Ilya Boyandin
 */
public class SmallMultiplesMain {

    private static final Font TITLE_FONT = new Font("Dialog", Font.BOLD, 64);
    private static final Color BACKGROUND_COLOR = new Color(0x60, 0x60, 0x60);

    public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {
        final int startYear = 2008;
//        final int endYear = 1988;
        final int endYear = 1975;
        final int n = Math.abs(endYear - startYear);
        final int numColumns = 4;
        final int paddingX = 10;
        final int paddingY = 5;
        final int frameWidth = 1024;
        final int frameHeight = 768;


        final String filenameTemplate = "data/refugees/refugees-{year}.xml";
        final DatasetSpec datasetSpec = new DatasetSpec(
                filenameTemplate.replace("{year}", Integer.toString(startYear)),
                "refugees", "x", "y", "name", "data/refugees/countries-areas.xml"
        );
        final String outputFileName = "refugees-small-multiples.png";



        final JFrame frame = new JFrame();
        final JFlowMap jFlowmap = new JFlowMap(null, datasetSpec, false);
        frame.add(jFlowmap);

        final ProgressMonitor progress = new ProgressMonitor(frame, "Rendering small multiples", "", 0, n);

        Dimension size = new Dimension(frameWidth, frameHeight);
        frame.setSize(size);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        jFlowmap.fitFlowMapInView();

        final int width = jFlowmap.getWidth();
        final int height = jFlowmap.getHeight();
        final int totalWidth = Math.min(n, numColumns) * (width + paddingX) + paddingX;
        final int totalHeight = ((int)Math.ceil((double)n / numColumns)) * (height + paddingY) + paddingY;



        BufferedImage image = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);

        final Graphics2D g = (Graphics2D)image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, totalWidth, totalHeight);

        for (int i = 0; i < n; i++) {
            final String year = Integer.toString(startYear + i * (startYear < endYear ? 1 : -1));
            final DatasetSpec ds = datasetSpec.withFilename(filenameTemplate.replace("{year}", year));
            final int I = i;
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    frame.setTitle(year);
//                    if (I > 0) {
                        jFlowmap.loadFlowMap(ds);
//                    }

                    FlowMapModel model = jFlowmap.getVisualFlowMap().getModel();
//                    model.setShowNodes(true);
                    model.setShowDirectionMarkers(true);
                    model.setDirectionMarkerSize(.19);
                    model.setDirectionMarkerAlpha(40);
                    model.setEdgeAlpha(40);
                }
            });
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    // Pain the plot
                    final int x = paddingX + (width + paddingX) * (I % numColumns);
                    final int y = paddingY + (height + paddingY) * (I / numColumns);

                    g.translate(x, y);

                    jFlowmap.paint(g);

                    g.setColor(Color.white);
                    g.setFont(TITLE_FONT);
                    g.drawString(year, 10, TITLE_FONT.getSize() + 7);

                    g.translate(-x, -y);

                    progress.setProgress(I);
                }
            });
        }

        ImageIO.write(image, FileUtils.getExtension(outputFileName), new File(outputFileName));
        System.exit(0);
    }
}
