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

package jflowmap.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import javax.swing.SwingUtilities;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.IView;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.GeoMap;
import jflowmap.models.map.MapArea;
import jflowmap.util.CollectionUtils;
import jflowmap.util.IOUtils;
import jflowmap.util.Pair;
import jflowmap.util.PropUtils;
import jflowmap.views.IFlowMapColorScheme;
import jflowmap.views.flowmap.FlowMapSmallMultipleView;
import jflowmap.views.flowmap.FlowMapView;
import jflowmap.views.flowmap.VisualFlowMapModel;
import jflowmap.views.flowstrates.FlowstratesView;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.util.io.IOLib;
import at.fhj.utils.misc.FileUtils;
import au.com.bytecode.opencsv.CSVParser;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class ViewConfig {

  private static Logger logger = Logger.getLogger(ViewConfig.class);

  public static final String PROP_INCLUDES = "includes";

  public static final String PROP_VIEW = "view";

  public static final String PROP_WINDOW = "window";
  public static final String PROP_WINDOW_TITLE = PROP_WINDOW + ".title";
  public static final String PROP_WINDOW_SIZE = PROP_WINDOW + ".size";
  public static final String PROP_WINDOW_SETTINGS = PROP_WINDOW + ".settings";
  public static final String PROP_WINDOW_SETTINGS_SHOW = PROP_WINDOW_SETTINGS + ".show";
  public static final String PROP_WINDOW_SETTINGS_ACTIVE_TAB = PROP_WINDOW_SETTINGS + ".activeTab";
  public static final String PROP_WINDOW_SETTINGS_SHOW_TABS = PROP_WINDOW_SETTINGS + ".showTabs";
  public static final String PROP_WINDOW_SETTINGS_EMBED = PROP_WINDOW_SETTINGS + ".embed";

  public static final String PROP_DATA = "data";

  public static final String PROP_DATA_SELECT = PROP_DATA + ".select";
  public static final String PROP_DATA_SELECT_NODES = PROP_DATA_SELECT + ".nodes.where";
  public static final String PROP_DATA_SELECT_FLOWS = PROP_DATA_SELECT + ".flows.where";
  public static final String PROP_DATA_SELECT_FLOWS_FORALL = PROP_DATA_SELECT_FLOWS + ".forAll";
  public static final String PROP_DATA_SELECT_FLOWS_EXISTS = PROP_DATA_SELECT_FLOWS + ".exists";

  public static final String PROP_DATA_CSV = PROP_DATA + ".csv";
  public static final String PROP_DATA_CSV_SEPARATOR = PROP_DATA_CSV + ".separator";
  public static final String PROP_DATA_CSV_CHARSET = PROP_DATA_CSV + ".charset";
  public static final String PROP_DATA_CSV_FLOWS = PROP_DATA_CSV + ".flows";
  public static final String PROP_DATA_CSV_NODES = PROP_DATA_CSV + ".nodes";
  public static final String PROP_DATA_CSV_FLOWS_SRC = PROP_DATA_CSV_FLOWS + ".src";
  public static final String PROP_DATA_CSV_NODES_SRC = PROP_DATA_CSV_NODES + ".src";
  public static final String PROP_DATA_ATTRS = PROP_DATA + ".attrs";
  public static final String PROP_DATA_ATTRS_NODE = PROP_DATA_ATTRS + ".node";
  public static final String PROP_DATA_ATTRS_NODE_ID = PROP_DATA_ATTRS_NODE + ".id";
  public static final String PROP_DATA_ATTRS_NODE_LABEL = PROP_DATA_ATTRS_NODE + ".label";
  public static final String PROP_DATA_ATTRS_NODE_LAT = PROP_DATA_ATTRS_NODE + ".lat";
  public static final String PROP_DATA_ATTRS_NODE_LON = PROP_DATA_ATTRS_NODE + ".lon";
  public static final String PROP_DATA_ATTRS_FLOW = PROP_DATA_ATTRS + ".flow";
  public static final String PROP_DATA_ATTRS_FLOW_ORIGIN = PROP_DATA_ATTRS_FLOW + ".origin";
  public static final String PROP_DATA_ATTRS_FLOW_DEST = PROP_DATA_ATTRS_FLOW + ".dest";
  public static final String PROP_DATA_ATTRS_FLOW_WEIGHT = PROP_DATA_ATTRS_FLOW + ".weight";
  public static final String PROP_DATA_ATTRS_FLOW_WEIGHT_RE = PROP_DATA_ATTRS_FLOW_WEIGHT + ".re";
  public static final String PROP_DATA_ATTRS_FLOW_WEIGHT_LIST = PROP_DATA_ATTRS_FLOW_WEIGHT + ".csvList";
  public static final String PROP_DATA_ATTRS_FLOW_WEIGHT_LEGEND_CAPTION = PROP_DATA_ATTRS_FLOW_WEIGHT + ".legendCaption";

  public static final String PROP_DATA_ATTRS_FLOW_WEIGHT_ATTRS_ATTR = PROP_DATA_ATTRS_FLOW_WEIGHT + ".weightAttrsAttr";

  public static final String PROP_DATA_GRAPHML = PROP_DATA + ".graphml";
  public static final String PROP_DATA_GRAPHML_SRC = PROP_DATA_GRAPHML + ".src";

  public static final String PROP_DATA_AGGREGATOR = PROP_DATA + ".aggregator";

  public static final String PROP_MAP = "map";
  public static final String PROP_MAP_PROJECTION = PROP_MAP + ".projection";
  public static final String PROP_MAP_XML = PROP_MAP + ".xml";
  public static final String PROP_MAP_XML_SRC = PROP_MAP_XML + ".src";
  public static final String PROP_MAP_SHAPEFILE = PROP_MAP + ".shapefile";
  public static final String PROP_MAP_SHAPEFILE_SRC = PROP_MAP_SHAPEFILE + ".src";
  public static final String PROP_MAP_SHAPEFILE_DBF_AREAIDFIELD_ = PROP_MAP_SHAPEFILE + ".dbfAreaIdField";
  public static final String PROP_MAP_SHAPEFILE_DBF_AREAIDFIELD = PROP_MAP_SHAPEFILE + ".dbf.areaIdField";
  public static final String PROP_MAP_SHAPEFILE_DBF_SELECT_SHAPES_WHERE = PROP_MAP_SHAPEFILE + ".dbf.select.shapes.where";

  public static final String PROP_MAP_BACKGROUND = PROP_MAP + ".background";
  public static final String PROP_MAP_BACKGROUND_SRC = PROP_MAP_BACKGROUND + ".src";
  public static final String PROP_MAP_BACKGROUND_OFFSET_X = PROP_MAP_BACKGROUND + ".offsetX";
  public static final String PROP_MAP_BACKGROUND_OFFSET_Y = PROP_MAP_BACKGROUND + ".offsetY";
  public static final String PROP_MAP_BACKGROUND_SCALE = PROP_MAP_BACKGROUND + ".scale";
  public static final String PROP_MAP_BACKGROUND_TRANSPARENCY = PROP_MAP_BACKGROUND + ".transparency";
  public static final String PROP_MAP_BACKGROUND_BOUNDING_BOX = PROP_MAP_BACKGROUND + ".boundingBox";

  private final Properties props;
  private final String location;
  private final ViewTypes viewType;
  private final DataLoaders dataLoader;
  private MapLoaders mapLoader;

  private ViewConfig(Properties props, String location) {
    this.props = props;
    this.location = location;
    this.viewType = ViewTypes.valueOf(require(PROP_VIEW).toUpperCase());
    this.dataLoader = DataLoaders.valueOf(require(PROP_DATA).toUpperCase());
    String mapLoaderName = getString(PROP_MAP);
    if (mapLoaderName != null  &&  mapLoaderName.trim().length() > 0) {
      this.mapLoader = MapLoaders.valueOf(mapLoaderName.toUpperCase());
    } else {
      this.mapLoader = null;
    }
  }

  public String getLocation() {
    return location;
  }

  public String getName() {
    return FileUtils.getFilename(location);
  }

  public IView createView() throws Exception {
    logger.info("Creating " + viewType + " view" +
    		" using " + dataLoader + " data loader" +
    	        " and " + (mapLoader != null ? mapLoader : "no") + " map loader");
    try {
      final Object data = dataLoader.load(this);
      final GeoMap mapModel = createMap();
      class ViewRef {
        IView view;
        Exception ex;
      }
      final ViewRef viewRef = new ViewRef();

      SwingUtilities.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          try {
            viewRef.view = viewType.createView(ViewConfig.this, data, mapModel);
          } catch (Exception e) {
            viewRef.ex = e;
          }
        }
      });

      if (viewRef.ex != null) {
        throw viewRef.ex;
      }
      return viewRef.view;
    } catch (Exception e) {
      error(e, location);  // will throw an IOException
      return null;
    }
  }

  private GeoMap createMap() throws IOException {
    if (mapLoader != null) {
      return mapLoader.load(this);
    } else {
      return new GeoMap("<Empty>", Collections.<MapArea>emptyList());
    }
  }

  public static ViewConfig load(String location) throws Exception {
    logger.info("Loading view config '" + location + "'");
    try {
      ViewConfig config = new ViewConfig(loadProps(location), location);
      return config;
    } catch (Exception e) {
      error(e, location);  // will throw an IOException
      return null;
    }
  }

  private static Properties loadProps(String location) throws IOException {
    InputStream is = null;
    is = IOUtils.asInputStream(location);

    Properties props = new Properties();
    props.load(is);

    // process includes
    if (props.containsKey(PROP_INCLUDES)) {
      for (String loc : props.getProperty(PROP_INCLUDES).split(",")) {
        Properties pps = loadProps(relativeFileLocation(loc, location));
        for (Map.Entry<Object, Object> e : pps.entrySet()) {
          if (!props.containsKey(e.getKey())) {  // properties set in the file which includes
                                                 // others have priority
            props.put(e.getKey(), e.getValue());
          }
        }
      }
    }

    return props;
  }

  private void error(String msg) throws Exception {
    error(msg, location, null);
  }

  private static void error(Exception cause, String location) throws Exception {
    error(cause.getMessage(), location, cause);
  }

  private static void error(String msg, String location, Exception cause) throws Exception {
    String longMsg = "Error loading view configuration from '" + location + "': " + msg;
    logger.error(longMsg, cause);
//    throw new IOException(longMsg, cause);
    throw cause;
  }

  public String require(String propName) {
    return PropUtils.require(props, propName);
  }

  public Pair<String, String> requireOneOf(String ... propNames) {
    return PropUtils.requireOneOf(props, propNames);
  }

  public String getString(String propName) {
    return PropUtils.getString(props, propName);
  }

  public String getString(String propName, boolean require) {
    return require ? PropUtils.require(props, propName) : getString(propName);
  }

  public String getStringOrElse(String propName, String defaultValue) {
    return PropUtils.getStringOrElse(props, propName, defaultValue);
  }

  public int getIntOrElse(String propName, int defaultValue) {
    return PropUtils.getIntOrElse(props, propName, defaultValue);
  }

  public double getDoubleOrElse(String propName, double defaultValue) {
    return PropUtils.getDoubleOrElse(props, propName, defaultValue);
  }

  public boolean getBoolOrElse(String propName, boolean defaultValue) {
    return PropUtils.getBoolOrElse(props, propName, defaultValue);
  }

  /**
   * @param loc Location relative to the current directory
   * @retun Location relative to this viewconf
   */
  public String relativeFileLocation(String loc) {
    return relativeFileLocation(loc, location);
  }

  public static String relativeFileLocation(String loc, String configLocation) {
    if (IOLib.isUrlString(loc)) {
      return loc;
    }

//    URL configUrl = IOLib.urlFromString(configLocation, null, true);

    // get path only
    String path = configLocation;
    int sep = Math.max(path.lastIndexOf(File.separator), path.lastIndexOf("/"));
    if (sep >= 0) {
      path = path.substring(0, sep + 1);
    }

    return path + loc;
  }

  enum DataLoaders {
    GRAPHML {
      @Override
      public Object load(ViewConfig config) throws IOException {
        final Graph graph = StaxGraphMLReader.readFirstGraph(
            config.relativeFileLocation(config.require(PROP_DATA_GRAPHML_SRC)));

        LazyGet<Iterable<String>> getFlowAttrs = new LazyGet<Iterable<String>>() {
          @Override
          public Iterable<String> get() { return FlowMapGraph.listFlowAttrs(graph); }
        };

        Iterable<String> attrs = weightAttrs(config, getFlowAttrs, true);

        FlowMapGraphBuilder.filterNodes(graph, config.getString(PROP_DATA_SELECT_NODES));
        FlowMapGraphBuilder.filterEdges(graph, config.getString(PROP_DATA_SELECT_FLOWS));
        FlowMapGraphBuilder.filterEdgesWithWeightAttrForAll(
            graph, config.getString(PROP_DATA_SELECT_FLOWS_FORALL), attrs);
        FlowMapGraphBuilder.filterEdgesWithWeightAttrExists(
            graph, config.getString(PROP_DATA_SELECT_FLOWS_EXISTS), attrs);


        return new FlowMapGraph(graph, createFlowMapAttrSpec(config, false, true, getFlowAttrs));
      }
    },
    CSV {
      @Override
      public Object load(ViewConfig config) throws IOException {
        return loadCsv(config, true);
      }

    },
    CSVDISAGGREGATED {
      @Override
      public Object load(ViewConfig config) throws IOException {
        return loadCsv(config, false);
      }
    }
    ;

    interface LazyGet<T> {
      T get() throws IOException;
    }

    public abstract Object load(ViewConfig config) throws IOException;


    public static Object loadCsv(ViewConfig config, boolean aggregated) throws IOException {
      final char csvSeparator = config.getStringOrElse(PROP_DATA_CSV_SEPARATOR, ",").charAt(0);
      final String csvCharset = config.getStringOrElse(PROP_DATA_CSV_CHARSET, "utf-8");

      final String nodesSrc = config.relativeFileLocation(config.require(PROP_DATA_CSV_NODES_SRC));
      final String flowsSrc = config.relativeFileLocation(config.require(PROP_DATA_CSV_FLOWS_SRC));

      FlowMapGraphBuilder builder;

      if (aggregated) {
        builder = CsvFlowMapGraphReader.readFlowMapGraph(
            nodesSrc,
            flowsSrc,
            createFlowMapAttrSpec(
                config, true, aggregated,
                new LazyGet<Iterable<String>>() {
                  @Override
                  public Iterable<String> get() throws IOException {
                    return CsvFlowMapGraphReader.readAttrNames(
                        flowsSrc,
                        csvSeparator, csvCharset);
                  }
                }
            ),
            csvSeparator, csvCharset);
      } else {

        final String weightAttrsAttr = config.require(PROP_DATA_ATTRS_FLOW_WEIGHT_ATTRS_ATTR);

        builder = CsvDisaggregatedFlowMapGraphReader.readFlowMapGraph(
            nodesSrc,
            flowsSrc,
            createFlowMapAttrSpec(
                config, true, aggregated,
                new LazyGet<Iterable<String>>() {
                  @Override
                  public Iterable<String> get() throws IOException {
                    return CsvDisaggregatedFlowMapGraphReader.readAttrNames(
                        flowsSrc, weightAttrsAttr,
                        csvSeparator, csvCharset);
                  }
                }
            ),
            weightAttrsAttr, csvSeparator, csvCharset);
      }

      String nodeFilter = config.getString(PROP_DATA_SELECT_NODES);
      String edgeFilter = config.getString(PROP_DATA_SELECT_FLOWS);
      String edgeFilterExists = config.getString(PROP_DATA_SELECT_FLOWS_EXISTS);
      String edgeFilterForAll = config.getString(PROP_DATA_SELECT_FLOWS_FORALL);

      if (!Strings.isNullOrEmpty(nodeFilter)) {
        builder.withNodeFilter(nodeFilter);
      }
      if (!Strings.isNullOrEmpty(edgeFilter)) {
        builder.withEdgeFilter(edgeFilter);
      }
      if (!Strings.isNullOrEmpty(edgeFilterExists)) {
        builder.withEdgeWeightAttrExistsFilter(edgeFilterExists);
      }
      if (!Strings.isNullOrEmpty(edgeFilterForAll)) {
        builder.withEdgeWeightAttrForAllFilter(edgeFilterForAll);
      }

      return builder.build();
    }

    private static FlowMapAttrSpec createFlowMapAttrSpec(ViewConfig config,
        boolean requireNodeIdAttrs, boolean aggregated,
        LazyGet<Iterable<String>> getFlowAttrs) throws IOException {
      return new FlowMapAttrSpec(
          config.getString(PROP_DATA_ATTRS_FLOW_ORIGIN, requireNodeIdAttrs),
          config.getString(PROP_DATA_ATTRS_FLOW_DEST, requireNodeIdAttrs),
          config.getString(PROP_DATA_ATTRS_FLOW_WEIGHT_LEGEND_CAPTION),
          weightAttrs(config, getFlowAttrs, aggregated),
          config.getString(PROP_DATA_ATTRS_NODE_ID, requireNodeIdAttrs),
          config.require(PROP_DATA_ATTRS_NODE_LABEL),
          config.require(PROP_DATA_ATTRS_NODE_LON),
          config.require(PROP_DATA_ATTRS_NODE_LAT));
    }

    private static Iterable<String> weightAttrs(ViewConfig config,
        LazyGet<Iterable<String>> getFlowAttrs, boolean aggregated)
      throws IOException
    {
      if (aggregated) {
        Pair<String, String> nameVal =
          config.requireOneOf(PROP_DATA_ATTRS_FLOW_WEIGHT_LIST, PROP_DATA_ATTRS_FLOW_WEIGHT_RE);

        String propName = nameVal.first();
        String propValue = nameVal.second();

        if (propName.equals(PROP_DATA_ATTRS_FLOW_WEIGHT_LIST)) {
          return Lists.<String>newArrayList(new CSVParser().parseLine(propValue));
        } else if (propName.equals(PROP_DATA_ATTRS_FLOW_WEIGHT_RE)) {
          return CollectionUtils.filterByPattern(getFlowAttrs.get(), propValue);
        } else {
          throw new UnsupportedOperationException("Unsupported property " + propName);
        }
      } else {
        config.require(PROP_DATA_ATTRS_FLOW_WEIGHT_ATTRS_ATTR);
        return getFlowAttrs.get();
      }
    }
  }

  enum ViewTypes {
    FLOWSTRATES {
      @Override
      public IView createView(ViewConfig config, Object data, GeoMap areaMap) throws Exception {
        return new FlowstratesView((FlowMapGraph)data, areaMap, mapProjection(config), config);
      }

    },
    FLOWMAP {
      @Override
      public IView createView(ViewConfig config, Object data, GeoMap areaMap) throws Exception {
        return new FlowMapView(
            VisualFlowMapModel.createFor((FlowMapGraph)data, config),
            areaMap, mapProjection(config), colorSchemeFor(config), config);
      }
    },
    FLOWMAPSMALLMULTIPLE {
      @Override
      public IView createView(ViewConfig config, Object data, GeoMap areaMap) throws Exception {
        return new FlowMapSmallMultipleView(
            VisualFlowMapModel.createFor((FlowMapGraph)data, config),
            areaMap, mapProjection(config), colorSchemeFor(config),
            config.getIntOrElse(FlowMapSmallMultipleView.VIEWCONF_NUM_OF_COLUMNS, 7)
        );
      }
    }
    ;

    public abstract IView createView(ViewConfig config, Object data, GeoMap areaMap)
      throws Exception;

    private static IFlowMapColorScheme colorSchemeFor(ViewConfig config) {
      return FlowMapColorSchemes.findByName(
          config.getStringOrElse(VisualFlowMapModel.VIEWCONF_COLOR_SCHEME, "Dark"));
    }

    private static MapProjections mapProjection(ViewConfig config) throws Exception {
      String projName = config.getStringOrElse(PROP_MAP_PROJECTION, "None").toUpperCase();
      MapProjections proj = MapProjections.valueOf(
          projName);
      if (proj == null) {
        config.error("Projection '" + projName + "' is not supported");
      }
      return proj;
    }

  }

  enum MapLoaders {
    XML {
      @Override
      public GeoMap load(ViewConfig config) throws IOException {
        return GeoMap.load(config.relativeFileLocation(config.require(PROP_MAP_XML_SRC)));
      }
    },
    SHAPEFILE {
      @Override
      public GeoMap load(ViewConfig config) throws IOException {
        return GeoMap.asAreaMap(ShapefileReader.loadShapefile(
            config.relativeFileLocation(config.require(PROP_MAP_SHAPEFILE_SRC)),
            config.getStringOrElse(PROP_MAP_SHAPEFILE_DBF_AREAIDFIELD, config.getString(PROP_MAP_SHAPEFILE_DBF_AREAIDFIELD_)),
            config.getString(PROP_MAP_SHAPEFILE_DBF_SELECT_SHAPES_WHERE)));
      }
    }
    ;

    public abstract GeoMap load(ViewConfig config) throws IOException;
  }

}
