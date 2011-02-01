package jflowmap.data;

import java.io.IOException;

import jflowmap.FlowMapGraph;
import jflowmap.geo.MapProjection;
import at.fhj.utils.misc.FileUtils;

/**
 * @author Ilya Boyandin
 */
public class CsvDatasetSpec extends DatasetSpec {

  private final String nodesCsv;
  private final String flowsCsv;
  private final String nodeIdAttr;
  private final String originNodeAttr;
  private final String destNodeAttr;

  public CsvDatasetSpec(
      String nodesCsv, String flowsCsv,
      String nodeIdAttr, String originNodeAttr, String destNodeAttr,

      String weightAttrNamePattern, String xNodeAttr, String yNodeAttr, String labelNodeAttr,
      String areaMapFilename, String shapefileName, String dbfAreaIdField, MapProjection proj) {

    super(FileUtils.getFilenameOnly(flowsCsv),
        weightAttrNamePattern, xNodeAttr, yNodeAttr, labelNodeAttr,
        areaMapFilename, shapefileName, dbfAreaIdField, proj);

    this.nodesCsv = nodesCsv;
    this.flowsCsv = flowsCsv;
    this.nodeIdAttr = nodeIdAttr;
    this.originNodeAttr = originNodeAttr;
    this.destNodeAttr = destNodeAttr;
  }

  @Override
  public FlowMapGraph load() throws IOException {
    Iterable<String> flowAttrs = CsvFlowMapGraphReader.readAttrNames(flowsCsv, ',', "utf-8");
    return CsvFlowMapGraphReader.readFlowMapGraph(
        nodesCsv, flowsCsv,
//        nodeIdAttr, originNodeAttr, destNodeAttr,
        createFlowMapAttrsSpecFor(flowAttrs), ',', "utf-8");
  }

}
