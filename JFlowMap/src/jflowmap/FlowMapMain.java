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

package jflowmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

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

import jflowmap.data.GraphMLReader3;
import jflowmap.ui.actions.OpenFileAction;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import at.fhj.utils.swing.InternalFrameUtils;
import at.fhj.utils.swing.JMemoryIndicator;

/**
 * @author Ilya Boyandin
 */
public class FlowMapMain extends JFrame {

  public static final String APP_NAME = "FlowMapView";
//  private static final String PREFERENCES_FILE_NAME = ".preferences";

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
//  private String appStartDir;
  private OpenFileAction openAsMapAction;
  private JComponent activeView;
  private AbstractAction tileViewsAction;

  private OpenFileAction openAsTimelineAction;


  public FlowMapMain() {
    setTitle("FlowMapView");

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
//    setPreferredSize(new Dimension(800, 600));
//    pack();

//    setAppStartDir(System.getProperty("user.dir"));
//    loadPreferences();

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
//    SwingUtilities.invokeLater(new Runnable() {
//      public void run() {
//        try {
////          showFlowTimeline("data/refugees/refugees_1975-2008.xml.gz");
//
//          showView(loadGraphsWithRegions("http://jflowmap.googlecode.com/svn/trunk/FlowMapView/data/refugees/refugees_1975-2008.xml.gz"));
//
////          desktopPane.getAllFrames()[0].setMaximum(true);
//        } catch (Exception ex) {
//          JMsgPane.showProblemDialog(FlowMapMain.this, "File couldn't be loaded: "
//              + ex.getMessage());
//          logger.error("Cant open file", ex);
//        }
//      }
//    });
  }

  private void initActions() {
    openAsMapAction = new OpenFileAction(this, OpenFileAction.As.MAP);
//    openAsTimelineAction = new OpenFileAction(this, OpenFileAction.As.TIMELINE);
    tileViewsAction = new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        InternalFrameUtils.tile(desktopPane);
      }
    };
  }

//
//  public void showFlowTimeline(String filename) throws DataIOException, XMLStreamException, IOException {
//    showView(loadGraphsWithRegions(filename));
//  }




  /*
  public void showFlowMaps(String filename) throws DataIOException, IOException {
    Iterable<Graph> graphs = loadFile(filename);

    // TODO: add support for links to external content in GraphML files (use <locator>)
    AreaMap areaMap = AreaMap.load("data/refugees/countries-areas.xml.gz");

    List<FlowMapView> flowMaps = Lists.newArrayList();
    List<JInternalFrame> iframes = Lists.newArrayList();
    for (Graph graph : graphs) {
      // TODO: let the user choose the attr specs
      FlowMapAttrSpec attrSpecs = REFUGEES_ATTR_SPECS;

      FlowMapView view = new FlowMapView(new FlowMapGraph(graph, attrSpecs), areaMap);
      view.getVisualFlowMap().setLegendVisible(false);
      JInternalFrame iframe = showView(view);

      flowMaps.add(view);
      iframes.add(iframe);
      iframe.toFront();
    }

    InternalFrameUtils.tile(iframes.toArray(new JInternalFrame[iframes.size()]));
    for (FlowMapView flowMap : flowMaps) {
      flowMap.fitInView();
    }
  }
  */


  private Iterable<Graph> loadFile(String filename) throws IOException {
    logger.info("Opening file: " + filename);
    Iterable<Graph> graphs = new GraphMLReader3().readFromLocation(filename);
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
//        updateActions();
      }

      @Override
      public void internalFrameDeactivated(InternalFrameEvent e) {
        setActiveView(null);
//        updateActions();
      }

      @Override
      public void internalFrameOpened(InternalFrameEvent e) {
//        openViews.add(view);
//        updateActions();
      }

      @Override
      public void internalFrameClosed(InternalFrameEvent e) {
//        openViews.remove(view);
//        view.setFrame(null);
//        updateActions();
      }
    });

    return iframe;
  }

  private void setActiveView(JComponent view) {
//    if (view != null) {
//      setViewOptionsDialogContent(view.getOptionsComponent());
//
//      if (activeView != null) {
//        removeToolbarActions(activeView.getToolbarActions());
//        removeToolbarControls(view.getToolbarControls());
//      }
//      addToolbarActions(view.getToolbarActions());
//      addToolbarControls(view.getToolbarControls());
//      actionsToolBar.repaint();
//    } else {
//      setViewOptionsDialogContent(null);
//      if (activeView != null) {
//        removeToolbarActions(activeView.getToolbarActions());
//        removeToolbarControls(activeView.getToolbarControls());
//        actionsToolBar.repaint();
//      }
//    }
    this.activeView = view;
  }

  private JMenuBar buildMenuBar() {
    final JMenuBar mb = new JMenuBar();
    JMenu menu, subMenu;
    JMenuItem item;

    menu = new JMenu("File");
    mb.add(menu);

    menu.add(openAsMapAction);
//    menu.add(openAsTimelineAction);

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
    logger.info(">>> Exiting FlowMapView");
//    savePreferences();
    System.exit(0);
  }

  public boolean confirmExit() {
    if (desktopPane.getAllFrames().length > 0) {
//      int confirm = JOptionPane.showConfirmDialog(this,
//          "Close all views and exit application?", "Exit",
//          JOptionPane.YES_NO_OPTION);
//      if (confirm == JOptionPane.YES_OPTION) {
        return true;
//      } else {
//        return false;
//      }
    }
    return true;
  }

//  public String getAppStartDir() {
//    return appStartDir;
//  }
//
//  public void setAppStartDir(String appStartDir) {
//    this.appStartDir = appStartDir;
//  }
//
//  protected void loadPreferences() {
//    final String fileName = getPreferencesFileName();
//    if (new File(fileName).isFile()) {
//      logger.info("Loading preferences from " + fileName);
//      try {
//        Preferences.importPreferences(new FileInputStream(fileName));
//      } catch (Throwable th) {
//        logger.error("Loading preferences failed", th);
//      }
//    }
//  }
//
//  protected void savePreferences() {
//    final String fileName = getPreferencesFileName();
//    logger.info("Saving preferences to " + fileName);
//    try {
//      Preferences.userRoot().exportSubtree(new FileOutputStream(fileName));
//    } catch (Throwable th) {
//      logger.error("Loading preferences failed", th);
//    }
//  }
//
//  protected String getPreferencesFileName() {
//    return getAppStartDir() + File.separator + PREFERENCES_FILE_NAME;
//  }

  private void exit() {
    if (confirmExit()) {
      dispose();
      shutdown();
    }
  }

  private void fitAllInViews() {
    JInternalFrame[] iframes = desktopPane.getAllFrames();
//    for (JInternalFrame iframe : iframes) {
//      Container view = iframe.getContentPane();
//      if (view instanceof FlowMapView)
//        ((FlowMapView)view).fitInView();
//    }
  }

  private void fitActiveInView() {
    if (activeView != null) {
//      if (activeView instanceof FlowMapView)
//        ((FlowMapView)activeView).fitInView();
    }
  }

  public static void main(String[] args) throws IOException {
    logger.info(">>> Starting FlowMapView");
//    final List<DatasetSpec> datasetSpecs = XmlDatasetSpecsReader.readDatasetSpecs("/data/datasets.xml");
    initLookAndFeel();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new FlowMapMain().setVisible(true);
      }
    });
  }

  private static void initLookAndFeel() {
    try {
      if (!IS_OS_MAC) {
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
          if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
          }
        }
      }
//      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
