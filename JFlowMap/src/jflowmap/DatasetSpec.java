package jflowmap;

import jflowmap.geo.MapProjection;
import jflowmap.geo.MapProjections;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import at.fhj.utils.misc.FileUtils;

/**
 * @author Ilya Boyandin
 */
public class DatasetSpec {

  private static Logger logger = Logger.getLogger(DatasetSpec.class);

  private final String filename;
  private final String name;
  private final String areaMapFilename;
  private final String shapefileName;
  private final String labelNodeAttr;
  private final String xNodeAttr, yNodeAttr;
  private final String weightAttrNamePattern;
  private final MapProjection mapProjection;
  private final String dbfAreaIdField;

  public DatasetSpec(String filename, String weightAttrNamePattern,
  		String xNodeAttr, String yNodeAttr,
  		String labelNodeAttr,
  		String areaMapFilename, String shapefileName,
  		String dbfAreaIdField, MapProjection proj) {
    this.weightAttrNamePattern = weightAttrNamePattern;
    this.filename = filename;
    this.name = FileUtils.getFilenameOnly(filename);
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

  public DatasetSpec withFilename(String filename) {
    return new DatasetSpec(filename, weightAttrNamePattern,
        xNodeAttr, yNodeAttr, labelNodeAttr, areaMapFilename, shapefileName,
        dbfAreaIdField, mapProjection);
  }

  public FlowMapAttrSpec createFlowMapAttrsSpecFor(Graph graph) {
    logger.info("Creating FlowMapAttrsSpec with attrNamePattern: " + weightAttrNamePattern);
    return new FlowMapAttrSpec(
        FlowMapGraph.findEdgeAttrsByPattern(graph, weightAttrNamePattern),
        labelNodeAttr, xNodeAttr,  yNodeAttr);
  }

  public MapProjection getMapProjection() {
    return mapProjection;
  }

  public String getFilename() {
    return filename;
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
}
