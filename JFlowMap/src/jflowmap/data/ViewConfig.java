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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphAggLayers;
import jflowmap.IView;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.GeoMap;
import jflowmap.models.map.MapArea;
import jflowmap.util.CollectionUtils;
import jflowmap.util.IOUtils;
import jflowmap.util.Pair;
import jflowmap.util.PropUtils;
import jflowmap.views.IFlowMapColorScheme;
import jflowmap.views.flowmap.FlowMapView;
import jflowmap.views.flowmap.VisualFlowMapModel;
import jflowmap.views.flowstrates.AggLayersBuilder;
import jflowmap.views.flowstrates.FlowstratesView;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import au.com.bytecode.opencsv.CSVParser;

import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class ViewConfig {

  private static Logger logger = Logger.getLogger(ViewConfig.class);

  public static final String PROP_VIEW = "view";

  public static final String PROP_DATA = "data";

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

  public static final String PROP_DATA_GRAPHML = PROP_DATA + ".graphml";
  public static final String PROP_DATA_GRAPHML_SRC = PROP_DATA_GRAPHML + ".src";

  public static final String PROP_DATA_AGGREGATOR = PROP_DATA + ".aggregator";

  public static final String PROP_MAP = "map";
  public static final String PROP_MAP_PROJECTION = PROP_MAP + ".projection";
  public static final String PROP_MAP_XML = PROP_MAP + ".xml";
  public static final String PROP_MAP_XML_SRC = PROP_MAP_XML + ".src";
  public static final String PROP_MAP_SHAPEFILE = PROP_MAP + ".shapefile";
  public static final String PROP_MAP_SHAPEFILE_SRC = PROP_MAP_SHAPEFILE + ".src";
  public static final String PROP_MAP_SHAPEFILE_DBFAREAIDFIELD = PROP_MAP_SHAPEFILE + ".dbfAreaIdField";

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

  public IView createView() throws IOException {
    logger.info("Creating " + viewType + " view" +
    		" using " + dataLoader + " data loader" +
    	        " and " + (mapLoader != null ? mapLoader : "no") + " map loader");
    try {
      return viewType.createView(this, dataLoader.load(this), createMap());
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

  public static ViewConfig load(String location) throws IOException {
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

    return props;
  }

  private void error(String msg) throws IOException {
    error(msg, location, null);
  }

  private static void error(Exception cause, String location) throws IOException {
    error(cause.getMessage(), location, cause);
  }

  private static void error(String msg, String location, Exception cause) throws IOException {
    String longMsg = "Error loading view configuration from '" + location + "': " + msg;
    logger.error(longMsg);
    throw new IOException(longMsg, cause);
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

  enum DataLoaders {
    CSV {
      @Override
      public Object load(final ViewConfig config) throws IOException {
        final char csvSeparator = config.getStringOrElse(PROP_DATA_CSV_SEPARATOR, ",").charAt(0);
        final String csvCharset = config.getStringOrElse(PROP_DATA_CSV_CHARSET, "utf-8");
        return CsvFlowMapGraphReader.readFlowMapGraph(
            config.require(PROP_DATA_CSV_NODES_SRC),
            config.require(PROP_DATA_CSV_FLOWS_SRC),
            createFlowMapAttrSpec(
                config, true,
                new LazyGet<Iterable<String>>() {
                  @Override
                  public Iterable<String> get() throws IOException {
                    return CsvFlowMapGraphReader.readAttrNames(
                        config.require(PROP_DATA_CSV_FLOWS_SRC),
                        csvSeparator, csvCharset);
                  }
                }
            ),
            csvSeparator, csvCharset);
      }
    },
    GRAPHML {
      @Override
      public Object load(ViewConfig config) throws IOException {
        final Graph graph = StaxGraphMLReader.readFirstGraph(config.require(PROP_DATA_GRAPHML_SRC));

        return new FlowMapGraph(graph,
            createFlowMapAttrSpec(
                config, false,
                new LazyGet<Iterable<String>>() {
                    @Override
                    public Iterable<String> get() { return FlowMapGraph.listFlowAttrs(graph); }
                }));
      }
    };

    interface LazyGet<T> {
      T get() throws IOException;
    }

    public abstract Object load(ViewConfig config) throws IOException;

    private static FlowMapAttrSpec createFlowMapAttrSpec(ViewConfig config,
        boolean requireNodeIdAttrs, LazyGet<Iterable<String>> getFlowAttrs) throws IOException {
      return new FlowMapAttrSpec(
          config.getString(PROP_DATA_ATTRS_FLOW_ORIGIN, requireNodeIdAttrs),
          config.getString(PROP_DATA_ATTRS_FLOW_DEST, requireNodeIdAttrs),
          weightAttrs(config, getFlowAttrs),
          config.getString(PROP_DATA_ATTRS_NODE_ID, requireNodeIdAttrs),
          config.require(PROP_DATA_ATTRS_NODE_LABEL),
          config.require(PROP_DATA_ATTRS_NODE_LON),
          config.require(PROP_DATA_ATTRS_NODE_LAT));
    }

    private static Iterable<String> weightAttrs(ViewConfig config, LazyGet<Iterable<String>> getFlowAttrs)
      throws IOException
    {
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
    }
  }

  enum ViewTypes {
    FLOWSTRATES {
      @Override
      public IView createView(ViewConfig config, Object data, GeoMap areaMap) throws IOException {
        String aggName = config.getString(PROP_DATA_AGGREGATOR);
        AggLayersBuilder aggregator;
        if (aggName != null) {
          aggregator = FlowMapGraphAggLayers.createBuilder(aggName);
        } else {
          aggregator = null;
        }

        return new FlowstratesView((FlowMapGraph)data, areaMap, aggregator,
            config.getIntOrElse(FlowstratesView.VIEW_CONFIG_PROP_MAX_VISIBLE_TUPLES, -1),
            mapProjection(config));
      }

    },
    FLOWMAP {
      @Override
      public IView createView(ViewConfig config, Object data, GeoMap areaMap) throws IOException {
        FlowMapGraph fmg = (FlowMapGraph)data;

        VisualFlowMapModel model = new VisualFlowMapModel(fmg);
        model.setEdgeAlpha(config.getIntOrElse(FlowMapView.VIEW_CONFIG_PROP_EDGE_OPACITY, model.getEdgeAlpha()));
        model.setMaxEdgeWidth(config.getDoubleOrElse(FlowMapView.VIEW_CONFIG_PROP_EDGE_WIDTH, model.getMaxEdgeWidth()));

        double minWeight = config.getDoubleOrElse(FlowMapView.VIEW_CONFIG_PROP_WEIGHT_FILTER_MIN, Double.NaN);
        if (!Double.isNaN(minWeight)) {
          model.setEdgeWeightFilterMin(minWeight);
        }
        double maxWeight = config.getDoubleOrElse(FlowMapView.VIEW_CONFIG_PROP_WEIGHT_FILTER_MAX, Double.NaN);
        if (!Double.isNaN(maxWeight)) {
          model.setEdgeWeightFilterMax(maxWeight);
        }

        double minLength = config.getDoubleOrElse(FlowMapView.VIEW_CONFIG_PROP_LENGTH_FILTER_MIN, Double.NaN);
        if (!Double.isNaN(minLength)) {
          model.setEdgeLengthFilterMin(minLength);
        }
        double maxLength = config.getDoubleOrElse(FlowMapView.VIEW_CONFIG_PROP_LENGTH_FILTER_MAX, Double.NaN);
        if (!Double.isNaN(maxLength)) {
          model.setEdgeLengthFilterMax(maxLength);
        }
        model.setShowDirectionMarkers(config.getBoolOrElse(FlowMapView.VIEW_CONFIG_PROP_SHOW_DIRECTION_MARKERS, true));
        model.setShowNodes(config.getBoolOrElse(FlowMapView.VIEW_CONFIG_PROP_SHOW_NODES, true));
        model.setFillEdgesWithGradient(config.getBoolOrElse(FlowMapView.VIEW_CONFIG_PROP_FILL_EDGES_WITH_GRADIENT, true));

        IFlowMapColorScheme colors = FlowMapColorSchemes.findByName(
            config.getStringOrElse(FlowMapView.VIEW_CONFIG_PROP_COLOR_SCHEME, "Dark"));

        FlowMapView flowMapView = new FlowMapView(model, areaMap, mapProjection(config), colors);

        return flowMapView;
      }
    }
//    , FLOWMAPSMALLMULTIPLES {
//      @Override
//      public IView createView(ViewConfig config, Object data, AreaMap areaMap) throws IOException {
//        return null;
//      }
//    }
    ;

    public abstract IView createView(ViewConfig config, Object data, GeoMap areaMap)
      throws IOException;

    private static MapProjections mapProjection(ViewConfig config) throws IOException {
      String projName = config.getStringOrElse(PROP_MAP_PROJECTION, "Mercator").toUpperCase();
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
        return GeoMap.load(config.require(PROP_MAP_XML_SRC));
      }
    },
    SHAPEFILE {
      @Override
      public GeoMap load(ViewConfig config) throws IOException {
        return GeoMap.asAreaMap(ShapefileReader.loadShapefile(
            config.require(PROP_MAP_SHAPEFILE_SRC),
            config.getString(PROP_MAP_SHAPEFILE_DBFAREAIDFIELD)));
      }
    }
    ;

    public abstract GeoMap load(ViewConfig config) throws IOException;
  }

}
