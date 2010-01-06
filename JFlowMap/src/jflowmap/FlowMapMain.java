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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import jflowmap.data.XmlDatasetSpecsReader;

import org.apache.log4j.Logger;

import at.fhj.utils.swing.JMemoryIndicator;

/**
 * @author Ilya Boyandin
 */
public class FlowMapMain extends JFrame {

    private static Logger logger = Logger.getLogger(FlowMapMain.class);

    private static final long serialVersionUID = 1L;
    public static final String OS_NAME = System.getProperty("os.name");
    public static boolean IS_OS_MAC = getOSMatches("Mac");

    private static boolean getOSMatches(String osNamePrefix) {
        if (OS_NAME == null) {
            return false;
        }
        return OS_NAME.startsWith(osNamePrefix);
    }

    private final JFlowMap flowMap;

    public FlowMapMain(List<DatasetSpec> datasetSpecs) {
        setTitle("JFlowMap");
        flowMap = new JFlowMap(this, datasetSpecs, true);
        add(flowMap);

        JPanel statusPanel = new JPanel(new BorderLayout());
        add(statusPanel, BorderLayout.SOUTH);

        JMemoryIndicator mi = new JMemoryIndicator(3000);
        statusPanel.add(mi, BorderLayout.EAST);
        mi.startUpdater();

        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));
//        setPreferredSize(new Dimension(800, 600));
//        pack();

        final Dimension size = getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int locX = (screen.width - size.width) / 2;
        final int locY = (screen.height - size.height) / 2;
        setLocation(locX, locY);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                flowMap.fitFlowMapInView();
            }
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                shutdown();
            }
        });
    }

    public void shutdown() {
        logger.info("Exiting application");
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        logger.info(">>> Starting JFlowMap");
        final List<DatasetSpec> datasetSpecs = XmlDatasetSpecsReader.readDatasetSpecs("/data/datasets.xml");
        initLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FlowMapMain(datasetSpecs).setVisible(true);
            }
        });
    }

    private static void initLookAndFeel() {
//        try {
//            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
////            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
