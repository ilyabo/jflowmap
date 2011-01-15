package jflowmap.data;

import java.io.IOException;

import jflowmap.FlowMapGraph;
import jflowmap.geo.MapProjection;
import at.fhj.utils.misc.FileUtils;

/**
 * @author Ilya Boyandin
 */
public class GraphMLDatasetSpec extends DatasetSpec {

  private final String filename;

  public GraphMLDatasetSpec(
      String filename,
      String weightAttrNamePattern, String xNodeAttr, String yNodeAttr, String labelNodeAttr,
      String areaMapFilename, String shapefileName, String dbfAreaIdField, MapProjection proj) {

    super(FileUtils.getFilenameOnly(filename),
        weightAttrNamePattern,
        xNodeAttr, yNodeAttr,
        labelNodeAttr, areaMapFilename,
        shapefileName,
        dbfAreaIdField, proj);

    this.filename = filename;
  }


  public String getFilename() {
    return filename;
  }

  public GraphMLDatasetSpec withFilename(String filename) {
    return new GraphMLDatasetSpec(filename, weightAttrNamePattern,
        xNodeAttr, yNodeAttr, labelNodeAttr, areaMapFilename, shapefileName,
        dbfAreaIdField, mapProjection);
  }

  @Override
  public FlowMapGraph load() throws IOException {
    return FlowMapGraph.loadGraphML(this);
  }
}
