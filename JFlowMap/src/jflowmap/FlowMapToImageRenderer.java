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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import jflowmap.bundling.ForceDirectedBundlerParameters;
import jflowmap.clustering.NodeDistanceMeasure;
import jflowmap.data.FlowMapStats;
import jflowmap.data.GraphMLDatasetSpec;
import jflowmap.data.MultiFlowMapStats;
import jflowmap.geo.MapProjections;
import jflowmap.util.SwingUtils;
import jflowmap.views.IFlowMapColorScheme;
import jflowmap.views.flowmap.FlowMapView;
import jflowmap.views.flowmap.VisualFlowMap;
import jflowmap.views.flowmap.VisualFlowMapModel;
import jflowmap.views.flowmap.VisualNode;

import org.apache.log4j.Logger;

import at.fhj.utils.misc.FileUtils;
import ch.unifr.dmlib.cluster.Linkages;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class FlowMapToImageRenderer extends JFrame {

  private static final long serialVersionUID = 1L;

  private static Logger logger = Logger.getLogger(FlowMapToImageRenderer.class);

//  private static final Font TITLE_FONT = new Font("Dialog", Font.BOLD, 32);
//  private static final Font LABEL_FONT = new Font("Dialog", Font.PLAIN, 5);
  private static final Font TITLE_FONT = new Font("Helvetica", Font.BOLD, 32);
  private static final Font LABEL_FONT = new Font("Helvetica", Font.PLAIN, 5);
  private Color datasetNameLabelColor = Color.gray;

  private double zoom = 1.0;
  private double translateX, translateY;

//  private static final double ZOOM_LEVEL = 2.0;
//  private static final double MOVE_DX = 120, MOVE_DY = -70;

//  private static final double MOVE_DY = -30;
//  private static final double MOVE_DX = -70;
//  private static final double MOVE_DY = -60;

  private boolean useGlobalVisualMappings = true;
  private int numColumns = 5;

  private int paddingX = 5;
  private int paddingY = 5;

  private final Map<String, GraphMLDatasetSpec> datasets;

  private final String outputFileName;
  private boolean showLegend = true;
  private final FlowMapView jFlowMap;

  public FlowMapToImageRenderer(String outputFileName, GraphMLDatasetSpec datasetSpec, String ... datasetNames) {
    this(outputFileName, datasetsMap(datasetSpec, datasetNames));
  }

  public FlowMapToImageRenderer(String outputFileName,  Map<String, GraphMLDatasetSpec> datasets) {
    this.outputFileName = outputFileName;
    this.datasets = datasets;

    jFlowMap = new FlowMapView(null, false);
    add(jFlowMap.getVisualCanvas());


    setSize(1024, 768);

    setBackground(new Color(0x60, 0x60, 0x60));

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public void makeFullscreen() {
    SwingUtils.makeFullscreen(this, false);
  }

  public void setPaddingX(int paddingX) {
    this.paddingX = paddingX;
  }

  public void setPaddingY(int paddingY) {
    this.paddingY = paddingY;
  }

  public void setNumColumns(int numColumns) {
    this.numColumns = numColumns;
  }

  public int getNumColumns() {
    return numColumns;
  }

  public void setShowLegend(boolean showLegend) {
    this.showLegend = showLegend;
  }

  public void setColorScheme(IFlowMapColorScheme colorScheme) {
    jFlowMap.setColorScheme(colorScheme);
  }

  public void setUseGlobalVisualMappings(boolean useGlobalVisualMappings) {
    this.useGlobalVisualMappings = useGlobalVisualMappings;
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
  }

  public void setTranslation(double dx, double dy) {
    this.translateX = dx;
    this.translateY = dy;
  }

  public void setDatasetNameLabelColor(Color datasetNameLabelColor) {
    this.datasetNameLabelColor = datasetNameLabelColor;
  }

  private FlowMapModelInitializer flowMapModelInitializer;
  private FDEBInitializer fdebInitializer;
  private Clusterer clusterer;

  public interface Clusterer {
    VisualFlowMapModel cluster(FlowMapView jFlowMap);
  }

  public interface FlowMapModelInitializer {
    void setupFlowMapModel(VisualFlowMapModel model);
  }

  public interface FDEBInitializer {
    void setupFDEB(ForceDirectedBundlerParameters bundlerParams);
  }

  public void setFlowMapModelInitializer(FlowMapModelInitializer flowMapModelInitializer) {
    this.flowMapModelInitializer = flowMapModelInitializer;
  }

  public void setClusterer(Clusterer clusterer) {
    this.clusterer = clusterer;
  }

  public void setFdebInitializer(FDEBInitializer fdebInitializer) {
    this.fdebInitializer = fdebInitializer;
  }

  private void cluster() {
    if (clusterer != null) {
      setupFlowMapModel(clusterer.cluster(jFlowMap));
    }
  }

  private void setupFlowMapModel(VisualFlowMapModel model) {
    if (flowMapModelInitializer != null) {
      flowMapModelInitializer.setupFlowMapModel(model);
    }
  }

  private void setupBundlerParams(ForceDirectedBundlerParameters bundlerParams) {
    if (fdebInitializer != null) {
      fdebInitializer.setupFDEB(bundlerParams);
    }
  }

  private static Map<String, GraphMLDatasetSpec> datasetsMap(GraphMLDatasetSpec datasetSpec, String... datasetNames) {
    Map<String, GraphMLDatasetSpec> datasets = Maps.newLinkedHashMap();
    for (String name : datasetNames) {
      datasets.put(name, datasetSpec.withFilename(datasetSpec.getFilename().replace("{name}", name)));
    }
    return datasets;
  }


  private class RenderTask extends SwingWorker<Void, Void> {

    private final ProgressMonitor progress;
    private final FlowMapView jFlowMap;
    private final int width;
    private final int height;
    private final int totalWidth;
    private final int totalHeight;
    private final BufferedImage image;
    private final JFrame parentFrame;

    public RenderTask(JFrame parent, FlowMapView jFlowMap) {
      this.jFlowMap = jFlowMap;
      this.parentFrame = parent;

      final int n = datasets.size();
      progress = new ProgressMonitor(parent, "Rendering", "", 0, n);

      width = jFlowMap.getVisualCanvas().getWidth();
      height = jFlowMap.getVisualCanvas().getHeight();
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

    private void renderFlowMap() throws InterruptedException, InvocationTargetException, IOException {
      final FlowMapStats stats;
      if (useGlobalVisualMappings) {
        // calc the global stats
        List<FlowMapGraph> gs = Lists.newArrayList();
        for (Map.Entry<String, GraphMLDatasetSpec> entry : datasets.entrySet()) {
          final String name = entry.getKey();
          progress.setNote("Gathering stats for " + name);
          gs.add(FlowMapGraph.loadGraphML(entry.getValue()));
        }
        stats = MultiFlowMapStats.createFor(gs);
      } else {
        stats = null;
      }


      final Graphics2D g = (Graphics2D)image.getGraphics();

      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(getBackground());
      g.fillRect(0, 0, totalWidth, totalHeight);


      int cycle = 0;
      for (Map.Entry<String, GraphMLDatasetSpec> entry : datasets.entrySet()) {
        final String name = entry.getKey();
        final GraphMLDatasetSpec ds = entry.getValue();

        if (progress.isCanceled()) {
          break;
        }
//        final String name = Integer.toString(startYear + i * yearStep);
//        final DatasetSpec ds = datasetSpec.withFilename(filenameTemplate.replace("{year}", name));
        final int _cycle = cycle;

        SwingUtilities.invokeAndWait(new Runnable() {

          @Override
          public void run() {
            parentFrame.setTitle(name);
            jFlowMap.load(ds, stats);

            VisualFlowMap visualFlowMap = jFlowMap.getVisualFlowMap();
            visualFlowMap.setLegendVisible(showLegend);
            VisualFlowMapModel model = visualFlowMap.getModel();
            setupFlowMapModel(model);

            if (clusterer != null) {
              cluster();
            }
            if (fdebInitializer != null) {
              FlowMapGraph fmg = visualFlowMap.getFlowMapGraph();
              ForceDirectedBundlerParameters bundlerParams = new ForceDirectedBundlerParameters(
                  fmg, visualFlowMap.getValueAttr());
              setupBundlerParams(bundlerParams);
              visualFlowMap.bundleEdges(bundlerParams);
            }
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
              camera.scaleViewAboutPoint(zoom, viewBounds.x + viewBounds.width / 2, viewBounds.y + viewBounds.height / 2);
              viewBounds = (PBounds) camera.getViewBounds().clone();
              viewBounds.x += translateX;
              viewBounds.y += translateY;
              camera.setViewBounds(viewBounds);
            }
          });
        }
        if (progress.isCanceled()) {
          break;
        }
//        if (USE_CLUSTER_EDGE_JOINING) {
//          SwingUtilities.invokeAndWait(new Runnable() {
//            @Override
//            public void run() {
//              jFlowMap.getVisualFlowMap().joinClusterEdges();
//            }
//          });
//          if (progress.isCanceled()) {
//            break;
//          }
//        }
        SwingUtilities.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            visualFlowMap.addChild(createDatasetNameLabel(name, visualFlowMap.getCamera().getViewBounds()));
//            visualFlowMap.addChild(createLabelsNode(name, visualFlowMap));

            // Pain the plot
            final int x = paddingX + (width + paddingX) * (_cycle % numColumns);
            final int y = paddingY + (height + paddingY) * (_cycle / numColumns);

            g.translate(x, y);

            jFlowMap.getVisualCanvas().paint(g);

            g.setColor(datasetNameLabelColor);
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
//      System.exit(0);
      dispose();
    }
  }

  public void start() {
    RenderTask task = new RenderTask(this, jFlowMap);
//    task.addPropertyChangeListener(this);
    task.execute();
  }

  private PNode createDatasetNameLabel(final String title, PBounds cameraBounds) {
    PText ptext = new PText(title);
    ptext.setX(cameraBounds.getX());
    ptext.setY(cameraBounds.getY() + cameraBounds.getHeight() - TITLE_FONT.getSize2D());
    ptext.setFont(TITLE_FONT);
    ptext.setTextPaint(datasetNameLabelColor);
    return ptext;
  }
//
//  private static PNode createLabelsNode(String year, VisualFlowMap visualFlowMap) {
//    PNode labelLayer = new PNode();
//    addLabelTextNode("Stateless", visualFlowMap, labelLayer);
//    addLabelTextNode("Various", visualFlowMap, labelLayer);
//    return labelLayer;
//  }
//
//
//  private static PText addLabelTextNode(String label, VisualFlowMap visualFlowMap, PNode labelLayer) {
//    VisualNode node = visualFlowMap.getVisualNodeByLabel(label);
//    PText ptext = new PText(node.getLabel());
//    ptext.setFont(LABEL_FONT);
//    ptext.setTextPaint(LABEL_COLOR);
//    double width = 20;
//    double height = LABEL_FONT.getSize2D();
//    ptext.setBounds(node.getX() - width/2, node.getY() + visualFlowMap.getModel().getNodeSize() * 1.1, width, height);
//    ptext.setJustification(JLabel.CENTER_ALIGNMENT);
//    labelLayer.addChild(ptext);
//    return ptext;
//  }


  public static String[] eachYear(int startYear, int endYear, int yearStep) {
    int n = ((endYear - startYear) / yearStep) + 1;
    String[] years = new String[n];
    for (int i = 0; i < n; i++) {
      years[i] = Integer.toString(startYear + i * yearStep);
    }
    return years;
  }


  public static final FlowMapToImageRenderer createSM_Experiment() {
    FlowMapToImageRenderer sm = new FlowMapToImageRenderer(
        "refugees-small-multiples.png",
        new GraphMLDatasetSpec(
//            "data/refugees-one-region/refugees-{name}.xml", "ritypnv", "x", "y", "name", "data/refugees/countries-areas.xml"
            "data/refugees/refugees-{name}.xml.gz",
            "ritypnv",
//            "r",
            "x", "y", "name", "data/refugees/countries-areas.xml.gz",
            null,
            null, MapProjections.MERCATOR
        ),
//        "1994", "1996", "2000", "2007", "2008"
//        "1994", "2000", "2007"
//        "1996", "2002", "2008"
//        "1996", "2000", "2008"
//        "2008"
        eachYear(1989, 2008, +1)
    );
//    sm.setFrameSize(1280, 1024);
//    sm.setFrameSize(800, 600);
//    sm.setFrameSize(640, 480);
    sm.setZoom(1.3);
    sm.setTranslation(30, -50);
    sm.setFlowMapModelInitializer(new FlowMapModelInitializer() {
      @Override
      public void setupFlowMapModel(VisualFlowMapModel model) {
        model.setMaxEdgeWidth(10);
//        model.setMaxEdgeWidth(15);
        model.setNodeSize(3);

//        if (useFdeb) {
//          model.setShowDirectionMarkers(false);
//          model.setEdgeAlpha(245);
//        } else {
          model.setShowDirectionMarkers(true);
          model.setDirectionMarkerSize(.17);
//          model.setDirectionMarkerAlpha(255);
          model.setDirectionMarkerAlpha(245);
          model.setEdgeAlpha(100);
//          model.setEdgeAlpha(150);
//        }


//       model.setEdgeWeightFilterMin(2500);
//       model.setEdgeLengthFilterMax(70);
//       model.setEdgeLengthFilterMin(300);

          model.setShowNodes(true);

//          model.setEdgeLengthFilterMax(75);
      }
    });
//    sm.setFdebInitializer(new FDEBInitializer() {
//      @Override
//      public void setupFDEB(ForceDirectedBundlerParameters bundlerParams) {
////        bundlerParams.setEdgeValueAffectsAttraction(true);
////        bundlerParams.setS(5);
//      }
//    });
//    sm.setClusterer(new Clusterer() {
//      @Override
//      public VisualFlowMapModel cluster(FlowMapView jFlowMap) {
//        VisualFlowMap visualFlowMap = jFlowMap.getVisualFlowMap();
//        visualFlowMap.clusterNodes(
//            NodeDistanceMeasure.COMMON_EDGES_IN_OUT_COMB, Linkages.<VisualNode>complete(), true);
//        visualFlowMap.setClusterDistanceThreshold(0.82);
////        visualFlowMap.setEuclideanClusterDistanceThreshold(55);
//        visualFlowMap.setEuclideanClusterDistanceThreshold(40);
//        visualFlowMap.joinClusterEdges();
//        return jFlowMap.getVisualFlowMap().getModel();
//      }
//    });
    return sm;
  }

  public static final FlowMapToImageRenderer createSM_TLBS_all() {
    FlowMapToImageRenderer sm = new FlowMapToImageRenderer(
        "refugees-small-multiples.png",
        new GraphMLDatasetSpec(
          "data/refugees/refugees-{name}.xml.gz", "ritypnv", "x", "y", "name",
          "data/refugees/countries-areas.xml.gz", null, null, MapProjections.MERCATOR),
//          eachYear(1993, 2008, +1));
        eachYear(1992, 2008, +2));
//    eachYear(1991, 2007, +2));
//    sm.setSize(640, 480);
//    sm.setSize(1280, 1024);
//    sm.setSize(1600, 1200);
    sm.setNumColumns(3);
//    sm.setSize(1024, 768);
//  sm.setSize(1280, 1024);
    sm.setZoom(1.2);
    sm.setPaddingX(10);
    sm.setTranslation(50, 0);
    sm.setUseGlobalVisualMappings(false);
    sm.setShowLegend(false);
//    sm.setColorScheme(FlowMapColorSchemes.DARK.getScheme());
    sm.setColorScheme(FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.getScheme());
    sm.setFlowMapModelInitializer(new FlowMapModelInitializer() {
      @Override
      public void setupFlowMapModel(VisualFlowMapModel model) {
        model.setMaxEdgeWidth(15);
        model.setNodeSize(3);
        model.setShowDirectionMarkers(true);
        model.setDirectionMarkerSize(.17);
        model.setDirectionMarkerAlpha(245);
        model.setEdgeAlpha(100);
        model.setShowNodes(true);
      }
    });
    return sm;
  }

  public static final FlowMapToImageRenderer createSM_TLBS_3years() {
    FlowMapToImageRenderer sm = new FlowMapToImageRenderer(
        "refugees-small-multiples_TLBS_3years_global.png",
        new GraphMLDatasetSpec(
          "data/refugees/refugees-{name}.xml.gz", "ritypnv", "x", "y", "name",
          "data/refugees/countries-areas.xml.gz", null, null, MapProjections.MERCATOR),
        "1996", "2000", "2008");
    sm.setSize(800, 600);
    sm.setZoom(1.3);
    sm.setTranslation(30, -50);
    sm.setUseGlobalVisualMappings(true);
    sm.setShowLegend(true);
    sm.setColorScheme(FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.getScheme());
    sm.setFlowMapModelInitializer(new FlowMapModelInitializer() {
      @Override
      public void setupFlowMapModel(VisualFlowMapModel model) {
        model.setMaxEdgeWidth(10);
        model.setNodeSize(3);
        model.setShowDirectionMarkers(true);
        model.setDirectionMarkerSize(.17);
        model.setDirectionMarkerAlpha(245);
        model.setEdgeAlpha(100);
        model.setShowNodes(true);
      }
    });
    return sm;
  }


  public static final FlowMapToImageRenderer createSM_TLBS_3years_OneRegion() {
    FlowMapToImageRenderer sm = new FlowMapToImageRenderer(
        "refugees-eu_global.png",
        new GraphMLDatasetSpec(
          "data/refugees-eu/refugees-{name}.xml", "ritypnv", "x", "y", "name",
          "data/refugees-eu/countries-areas.xml", null,  null, MapProjections.MERCATOR),
        "1996", "2000", "2008");
    sm.setSize(800, 600);
    sm.setZoom(1.3);
    sm.setTranslation(30, -50);
    sm.setUseGlobalVisualMappings(true);
    sm.setShowLegend(true);
    sm.setColorScheme(FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.getScheme());
    sm.setFlowMapModelInitializer(new FlowMapModelInitializer() {
      @Override
      public void setupFlowMapModel(VisualFlowMapModel model) {
        model.setMaxEdgeWidth(10);
        model.setNodeSize(3);
        model.setShowDirectionMarkers(true);
        model.setDirectionMarkerSize(.17);
        model.setDirectionMarkerAlpha(245);
        model.setEdgeAlpha(100);
        model.setShowNodes(true);
      }
    });
    return sm;
  }



  public static final FlowMapToImageRenderer createSM_TLBS_bundled() {
    FlowMapToImageRenderer sm = new FlowMapToImageRenderer(
        "bundled.png",
        new GraphMLDatasetSpec(
          "data/refugees/refugees-{name}.xml.gz", "ritypnv", "x", "y", "name",
          "data/refugees/countries-areas.xml.gz", null,  null, MapProjections.MERCATOR),
//        "1996", "2000", "2008"
          "2000"
        );
    sm.setSize(1024, 768);
    sm.setZoom(1.3);
    sm.setBackground(Color.white);
    sm.setTranslation(30, -50);
    sm.setUseGlobalVisualMappings(true);
    sm.setShowLegend(true);
    sm.setColorScheme(FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.getScheme());
    sm.setFlowMapModelInitializer(new FlowMapModelInitializer() {
      @Override
      public void setupFlowMapModel(VisualFlowMapModel model) {
        model.setMaxEdgeWidth(10);
        model.setNodeSize(3);
        model.setShowDirectionMarkers(true);
        model.setDirectionMarkerSize(.17);
        model.setDirectionMarkerAlpha(245);
        model.setEdgeAlpha(100);
        model.setShowNodes(true);
      }
    });
    sm.setFdebInitializer(new FDEBInitializer() {
    @Override
    public void setupFDEB(ForceDirectedBundlerParameters bundlerParams) {
//      bundlerParams.setEdgeValueAffectsAttraction(true);
//      bundlerParams.setS(5);
    }
    });
    return sm;
  }

  public static final FlowMapToImageRenderer createSM_TLBS_clustered() {
    FlowMapToImageRenderer sm = new FlowMapToImageRenderer(
        "clustered.png",
        new GraphMLDatasetSpec(
          "data/refugees/refugees-{name}.xml.gz", "ritypnv", "x", "y", "name",
          "data/refugees/countries-areas.xml.gz", null,  null, MapProjections.MERCATOR),
//          "1996", "2000", "2008"
          "2000"
          );
    sm.setSize(1024, 768);
    sm.setBackground(Color.white);
    sm.setZoom(1.3);
    sm.setTranslation(30, -20);
    sm.setUseGlobalVisualMappings(true);
    sm.setShowLegend(true);
    sm.setColorScheme(FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.getScheme());
    sm.setFlowMapModelInitializer(new FlowMapModelInitializer() {
      @Override
      public void setupFlowMapModel(VisualFlowMapModel model) {
        model.setMaxEdgeWidth(10);
        model.setNodeSize(3);
        model.setShowDirectionMarkers(true);
        model.setDirectionMarkerSize(.17);
        model.setDirectionMarkerAlpha(245);
        model.setEdgeAlpha(100);
        model.setShowNodes(true);
      }
    });
      sm.setClusterer(new Clusterer() {
      @Override
      public VisualFlowMapModel cluster(FlowMapView jFlowMap) {
        VisualFlowMap visualFlowMap = jFlowMap.getVisualFlowMap();
        visualFlowMap.clusterNodes(
            NodeDistanceMeasure.COMMON_EDGES_IN_OUT_COMB, Linkages.<VisualNode>complete(), true);
        visualFlowMap.setClusterDistanceThreshold(0.82);
//        visualFlowMap.setEuclideanClusterDistanceThreshold(55);
        visualFlowMap.setEuclideanClusterDistanceThreshold(40);
        visualFlowMap.joinClusterEdges();
        return jFlowMap.getVisualFlowMap().getModel();
      }
      });

    return sm;
  }



  public static final FlowMapToImageRenderer createSM_eurovisPoster_6years_global() {
    FlowMapToImageRenderer sm = new FlowMapToImageRenderer(
        "refugees-small-multiples_eurovisPoster_6years_global.png",
        new GraphMLDatasetSpec(
          "data/refugees/refugees-{name}.xml.gz", "ritypnv", "x", "y", "name",
          "data/refugees/countries-areas.xml.gz", null,  null, MapProjections.MERCATOR),
        "1996", "1998", "2003", "2004", "2005", "2008");
    sm.setSize(1150, 900);
    sm.setZoom(1.3);
    sm.setNumColumns(3);
    sm.setTranslation(30,  0);
    sm.setUseGlobalVisualMappings(true);
    sm.setShowLegend(true);
    sm.setColorScheme(FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.getScheme());
    sm.setFlowMapModelInitializer(new FlowMapModelInitializer() {
      @Override
      public void setupFlowMapModel(VisualFlowMapModel model) {
        model.setMaxEdgeWidth(15);
        model.setNodeSize(3);
        model.setShowDirectionMarkers(true);
        model.setDirectionMarkerSize(.17);
        model.setDirectionMarkerAlpha(255);
        model.setEdgeAlpha(120);
        model.setShowNodes(true);
      }
    });
    return sm;
  }

  public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {
//    SmallMultiplesMain sm = createSM_eurovisPoster_6years_global();
    FlowMapToImageRenderer sm = createSM_TLBS_all();
    sm.setVisible(true);
    sm.start();
  }
}
