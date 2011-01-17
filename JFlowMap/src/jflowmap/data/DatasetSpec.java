package jflowmap.data;

import java.io.IOException;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;
import jflowmap.geo.MapProjection;
import jflowmap.geo.MapProjections;
import jflowmap.util.CollectionUtils;

import org.apache.log4j.Logger;

import prefuse.data.Graph;

/**
 * @author Ilya Boyandin
 */
public abstract class DatasetSpec {

  private static Logger logger = Logger.getLogger(GraphMLDatasetSpec.class);

  protected final String name;
  protected final String areaMapFilename;
  protected final String shapefileName;
  protected final String labelNodeAttr;
  protected final String xNodeAttr, yNodeAttr;
  protected final String weightAttrNamePattern;
  protected final MapProjection mapProjection;
  protected final String dbfAreaIdField;

  public DatasetSpec(
      String name,
      String weightAttrNamePattern,
                String xNodeAttr, String yNodeAttr,
                String labelNodeAttr,
                String areaMapFilename, String shapefileName,
                String dbfAreaIdField, MapProjection proj) {
    this.weightAttrNamePattern = weightAttrNamePattern;
    this.name = name;
    this.areaMapFilename = areaMapFilename;
    this.shapefileName = shapefileName;
    this.dbfAreaIdField = dbfAreaIdField;
    this.xNodeAttr = xNodeAttr;
    this.yNodeAttr = yNodeAttr;
    this.labelNodeAttr = labelNodeAttr;
    this.mapProjection = proj == null ?  MapProjections.NONE : proj;
  }

//  public DatasetSpec(String filename, String areaMapFilename, FlowMapAttrSpec attrsSpec) {
//    this.filename = filename;
//    this.name = FileUtils.getFilenameOnly(filename);
//    this.areaMapFilename = areaMapFilename;
//    this.attrsSpec = attrsSpec;
//  }


  public FlowMapAttrSpec createFlowMapAttrsSpecFor(Graph graph) {
//    return new FlowMapAttrSpec(
//        FlowMapGraph.findEdgeAttrsByPattern(graph, weightAttrNamePattern),
//        labelNodeAttr, xNodeAttr,  yNodeAttr);
    return createFlowMapAttrsSpecFor(FlowMapGraph.listFlowAttrs(graph));
  }

  public FlowMapAttrSpec createFlowMapAttrsSpecFor(Iterable<String> flowAttrs) {
    logger.info("Creating FlowMapAttrsSpec with attrNamePattern: " + weightAttrNamePattern);

    Iterable<String> weightAttrs =
      CollectionUtils.filterByPattern(flowAttrs, weightAttrNamePattern);

    return new FlowMapAttrSpec(weightAttrs, labelNodeAttr, xNodeAttr,  yNodeAttr);
  }

  public MapProjection getMapProjection() {
    return mapProjection;
  }

  public String getName() {
    return name;
  }

  public String getAreaMapFilename() {
    return areaMapFilename;
  }

  public String getShapefileName() {
    return shapefileName;
  }

  @Override
  public String toString() {
    return getName();
  }

  public String getDbfAreaIdField() {
    return dbfAreaIdField;
  }

  public abstract FlowMapGraph load() throws IOException;
}
