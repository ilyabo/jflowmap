package jflowmap;

import prefuse.data.Graph;
import at.fhj.utils.misc.FileUtils;

/**
 * @author Ilya Boyandin
 */
public class DatasetSpec {

  private final String filename;
  private final String name;
  private final String areaMapFilename;
  private final String labelNodeAttr;
  private final String xNodeAttr, yNodeAttr;
  private final String weightAttrNamePattern;


  public DatasetSpec(String filename, String weightAttrNamePattern,
  		String xNodeAttr, String yNodeAttr,
  		String labelNodeAttr, String areaMapFilename) {
    this.weightAttrNamePattern = weightAttrNamePattern;
    this.filename = filename;
    this.name = FileUtils.getFilenameOnly(filename);
    this.areaMapFilename = areaMapFilename;
    this.xNodeAttr = xNodeAttr;
    this.yNodeAttr = yNodeAttr;
    this.labelNodeAttr = labelNodeAttr;
  }

//  public DatasetSpec(String filename, String areaMapFilename, FlowMapAttrSpec attrsSpec) {
//    this.filename = filename;
//    this.name = FileUtils.getFilenameOnly(filename);
//    this.areaMapFilename = areaMapFilename;
//    this.attrsSpec = attrsSpec;
//  }

  public DatasetSpec withFilename(String filename) {
    return new DatasetSpec(filename, weightAttrNamePattern,
        xNodeAttr, yNodeAttr, labelNodeAttr, areaMapFilename);
  }

  public FlowMapAttrSpec createFlowMapAttrsSpecFor(Graph graph) {
    return new FlowMapAttrSpec(
        FlowMapGraph.findEdgeAttrsByPattern(graph, weightAttrNamePattern),
        labelNodeAttr, xNodeAttr,  yNodeAttr);
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

  @Override
  public String toString() {
    return getName();
  }
}
