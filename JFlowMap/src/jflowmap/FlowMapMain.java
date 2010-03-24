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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import jflowmap.data.FlowMapLoader;
import jflowmap.data.GraphMLReader2;
import jflowmap.data.XmlRegionsReader;
import jflowmap.geom.Point;
import jflowmap.models.FlowMapGraphBuilder;
import jflowmap.models.map.AreaMap;
import jflowmap.ui.actions.OpenFileAction;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.io.DataIOException;
import prefuse.util.ColorLib;
import at.fhj.utils.swing.InternalFrameUtils;
import at.fhj.utils.swing.JMemoryIndicator;
import at.fhj.utils.swing.JMsgPane;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Ilya Boyandin
 */
public class FlowMapMain extends JFrame {

    private final static FlowMapAttrsSpec REFUGEES_ATTR_SPECS = new FlowMapAttrsSpec(
//            "ritypnv",
          "rityp",
//          "r",
          "name", "x", "y", 0);


    public static final String APP_NAME = "JFlowMap";
//    private static final String PREFERENCES_FILE_NAME = ".preferences";

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

    private final JDesktopPane desktopPane;
//    private String appStartDir;
    private OpenFileAction openAsMapAction;
    private JComponent activeView;
    private AbstractAction tileViewsAction;

    private OpenFileAction openAsTimelineAction;

    public FlowMapMain() {

        setTitle("JFlowMap");

        initActions();

        setJMenuBar(buildMenuBar());

        desktopPane = new JDesktopPane();
        desktopPane.setBackground(Color.gray);
        add(desktopPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        add(statusPanel, BorderLayout.SOUTH);

        JMemoryIndicator mi = new JMemoryIndicator(3000);
        statusPanel.add(mi, BorderLayout.EAST);
        mi.startUpdater();

        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));
//        setPreferredSize(new Dimension(800, 600));
//        pack();

//        setAppStartDir(System.getProperty("user.dir"));
//        loadPreferences();

        final Dimension size = getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int locX = (screen.width - size.width) / 2;
        final int locY = (screen.height - size.height) / 2;
        setLocation(locX, locY);


        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
            @Override
            public void windowOpened(WindowEvent e) {
                InternalFrameUtils.tile(desktopPane);
            }
        });


        // TODO: remove hardcoded filename to open
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    showFlowTimeline("data/refugees/refugees_1975-2008.xml.gz");

//                    desktopPane.getAllFrames()[0].setMaximum(true);
                } catch (Exception ex) {
                    JMsgPane.showProblemDialog(FlowMapMain.this, "File couldn't be loaded: "
                            + ex.getMessage());
                    logger.error("Cant open file", ex);
                }
            }
        });
    }

    private void initActions() {
        openAsMapAction = new OpenFileAction(this, OpenFileAction.As.MAP);
        openAsTimelineAction = new OpenFileAction(this, OpenFileAction.As.TIMELINE);
        tileViewsAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                InternalFrameUtils.tile(desktopPane);
            }
        };
    }


    public void showFlowTimeline(String filename) throws Exception {
        GraphMLReader2 reader = new GraphMLReader2();
        List<Graph> graphs = Lists.newArrayList(reader.readFromFile(filename));

        Collections.reverse(graphs);

        Map<String, String> countryToRegion = XmlRegionsReader.readFrom("data/refugees/regions.xml");
        Set<String> regions = Sets.newLinkedHashSet(countryToRegion.values());
        Map<String, Integer> colorMap = createColorMapForRegions(countryToRegion);

        // TODO: let the user choose the attr specs
        showView(new JFlowTimeline(graphs, REFUGEES_ATTR_SPECS, countryToRegion, colorMap));


        Map<String, String> regionToRegion = Maps.newLinkedHashMap();
        for (String r : regions) {
            regionToRegion.put(r, r);
        }

        List<Graph> regionSummaryGraphs = Lists.newArrayList();
        for (Graph g : graphs) {

            FlowMapGraphBuilder builder = new FlowMapGraphBuilder(FlowMapLoader.getGraphId(g))
//                .withCumulativeEdges()            // TODO: why isn't it working?
                .withNodeXAttr(REFUGEES_ATTR_SPECS.getXNodeAttr())
                .withNodeYAttr(REFUGEES_ATTR_SPECS.getYNodeAttr())
                .withEdgeWeightAttr(REFUGEES_ATTR_SPECS.getEdgeWeightAttr())
                .withNodeLabelAttr(REFUGEES_ATTR_SPECS.getNodeLabelAttr())
                ;

            Map<String, Node> regionToNode = Maps.newHashMap();
            for (String region : regions) {
                Node node = builder.addNode(region, new Point(0, 0), region);
                regionToNode.put(region, node);
            }

            for (int i = 0, numEdges = g.getEdgeCount(); i < numEdges; i++) {
                Edge e = g.getEdge(i);
                String src = e.getSourceNode().getString(JFlowTimeline.NODE_COLUMN__REGION);
                String trg = e.getTargetNode().getString(JFlowTimeline.NODE_COLUMN__REGION);
                if (src == null) {
                    throw new IllegalArgumentException("No region for " + e.getSourceNode());
                }
                if (trg == null) {
                    throw new IllegalArgumentException("No region for " + e.getTargetNode());
                }
                builder.addEdge(
                        regionToNode.get(src),
                        regionToNode.get(trg),
                        e.getDouble(REFUGEES_ATTR_SPECS.getEdgeWeightAttr()));
            }

            regionSummaryGraphs.add(builder.build());
        }

        showView(new JFlowTimeline(regionSummaryGraphs, REFUGEES_ATTR_SPECS, regionToRegion, colorMap));

    }


    private Map<String, Integer> createColorMapForRegions(Map<String, String> countryToRegion) {
        HashSet<String> regions = Sets.newHashSet(countryToRegion.values());
        int[] palette = ColorLib.getCategoryPalette(regions.size(), 1.f, 0.4f, 1.f, .15f);

        int colorIdx = 0;
        Map<String, Integer> regionToColor = Maps.newHashMap();
        for (String region : regions) {
            regionToColor.put(region, palette[colorIdx]);
            colorIdx++;
        }

        return regionToColor;
    }

    public void showFlowMaps(String filename) throws DataIOException, IOException {
        Iterable<Graph> graphs = loadFile(filename);

        // TODO: add support for links to external content in GraphML files (use <locator>)
        AreaMap areaMap = AreaMap.load("data/refugees/countries-areas.xml.gz");

        List<JFlowMap> flowMaps = Lists.newArrayList();
        List<JInternalFrame> iframes = Lists.newArrayList();
        for (Graph graph : graphs) {
            // TODO: let the user choose the attr specs
            FlowMapAttrsSpec attrSpecs = REFUGEES_ATTR_SPECS;

            JFlowMap view = new JFlowMap(graph, attrSpecs, areaMap);
            view.getVisualFlowMap().setLegendVisible(false);
            JInternalFrame iframe = showView(view);

            flowMaps.add(view);
            iframes.add(iframe);
            iframe.toFront();
        }

        InternalFrameUtils.tile(iframes.toArray(new JInternalFrame[iframes.size()]));
        for (JFlowMap flowMap : flowMaps) {
            flowMap.fitFlowMapInView();
        }
    }

    private Iterable<Graph> loadFile(String filename) throws DataIOException {
        logger.info("Opening file: " + filename);
        Iterable<Graph> graphs = new GraphMLReader2().readFromFile(filename);
        return graphs;
    }

    private JInternalFrame showView(final JComponent view) {
        JInternalFrame iframe = new JInternalFrame(view.getName(), true, true, true, true);
        iframe.setContentPane(view);
        view.setPreferredSize(new Dimension(800, 600));
        final int offset = desktopPane.getAllFrames().length * 16;
        iframe.setLocation(offset, offset);
        iframe.pack();
        iframe.setVisible(true);
        desktopPane.add(iframe);

        iframe.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                setActiveView(view);
//                updateActions();
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                setActiveView(null);
//                updateActions();
            }

            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
//                openViews.add(view);
//                updateActions();
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
//                openViews.remove(view);
//                view.setFrame(null);
//                updateActions();
            }
        });

        return iframe;
    }

    private void setActiveView(JComponent view) {
//        if (view != null) {
//            setViewOptionsDialogContent(view.getOptionsComponent());
//
//            if (activeView != null) {
//                removeToolbarActions(activeView.getToolbarActions());
//                removeToolbarControls(view.getToolbarControls());
//            }
//            addToolbarActions(view.getToolbarActions());
//            addToolbarControls(view.getToolbarControls());
//            actionsToolBar.repaint();
//        } else {
//            setViewOptionsDialogContent(null);
//            if (activeView != null) {
//                removeToolbarActions(activeView.getToolbarActions());
//                removeToolbarControls(activeView.getToolbarControls());
//                actionsToolBar.repaint();
//            }
//        }
        this.activeView = view;
    }

    private JMenuBar buildMenuBar() {
        final JMenuBar mb = new JMenuBar();
        JMenu menu, subMenu;
        JMenuItem item;

        menu = new JMenu("File");
        mb.add(menu);

        menu.add(openAsMapAction);
        menu.add(openAsTimelineAction);

        // TODO: recent files
        menu.addSeparator();

        item = new JMenuItem("Exit");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        menu.add(item);



        menu = new JMenu("Window");
        mb.add(menu);

        item = new JMenuItem("Fit in view");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fitActiveInView();
            }
        });
        menu.add(item);

        item = new JMenuItem("Fit all in views");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fitAllInViews();
            }
        });
        menu.add(item);

        menu.addSeparator();

        subMenu = new JMenu("Arrange");
        menu.add(subMenu);

        item = new JMenuItem("Cascade");
        item.setEnabled(false);
        subMenu.add(item);
        item = new JMenuItem("Tile");
        item.addActionListener(tileViewsAction);
        subMenu.add(item);

        item = new JMenuItem("Tile horizontally");
        item.setEnabled(false);
        subMenu.add(item);
        item = new JMenuItem("Tile vertically");
        item.setEnabled(false);
        subMenu.add(item);
        item = new JMenuItem("Maximize all");
        item.setEnabled(false);
        subMenu.add(item);
        item = new JMenuItem("Minimize all");
        item.setEnabled(false);
        subMenu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Preferences...");
        item.setEnabled(false);
        menu.add(item);

        menu = new JMenu("Help");
        mb.add(menu);

        item = new JMenuItem("About");
        menu.add(item);

        return mb;
    }

    public void shutdown() {
        logger.info(">>> Exiting JFlowMap");
//        savePreferences();
        System.exit(0);
    }

    public boolean confirmExit() {
        if (desktopPane.getAllFrames().length > 0) {
//            int confirm = JOptionPane.showConfirmDialog(this,
//                    "Close all views and exit application?", "Exit",
//                    JOptionPane.YES_NO_OPTION);
//            if (confirm == JOptionPane.YES_OPTION) {
                return true;
//            } else {
//                return false;
//            }
        }
        return true;
    }

//    public String getAppStartDir() {
//        return appStartDir;
//    }
//
//    public void setAppStartDir(String appStartDir) {
//        this.appStartDir = appStartDir;
//    }
//
//    protected void loadPreferences() {
//        final String fileName = getPreferencesFileName();
//        if (new File(fileName).isFile()) {
//            logger.info("Loading preferences from " + fileName);
//            try {
//                Preferences.importPreferences(new FileInputStream(fileName));
//            } catch (Throwable th) {
//                logger.error("Loading preferences failed", th);
//            }
//        }
//    }
//
//    protected void savePreferences() {
//        final String fileName = getPreferencesFileName();
//        logger.info("Saving preferences to " + fileName);
//        try {
//            Preferences.userRoot().exportSubtree(new FileOutputStream(fileName));
//        } catch (Throwable th) {
//            logger.error("Loading preferences failed", th);
//        }
//    }
//
//    protected String getPreferencesFileName() {
//        return getAppStartDir() + File.separator + PREFERENCES_FILE_NAME;
//    }

    private void exit() {
        if (confirmExit()) {
            dispose();
            shutdown();
        }
    }

    private void fitAllInViews() {
        JInternalFrame[] iframes = desktopPane.getAllFrames();
        for (JInternalFrame iframe : iframes) {
            Container view = iframe.getContentPane();
            if (view instanceof JFlowMap)
                ((JFlowMap)view).fitFlowMapInView();
        }
    }

    private void fitActiveInView() {
        if (activeView != null) {
            if (activeView instanceof JFlowMap)
                ((JFlowMap)activeView).fitFlowMapInView();
        }
    }

    public static void main(String[] args) throws IOException {
        logger.info(">>> Starting JFlowMap");
//        final List<DatasetSpec> datasetSpecs = XmlDatasetSpecsReader.readDatasetSpecs("/data/datasets.xml");
        initLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FlowMapMain().setVisible(true);
            }
        });
    }

    private static void initLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
