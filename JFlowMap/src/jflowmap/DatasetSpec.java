package jflowmap;

import at.fhj.utils.misc.FileUtils;

/**
 * @author Ilya Boyandin
 */
public class DatasetSpec {

  private final String filename;
  private final String name;
  private final String areaMapFilename;
  private final FlowMapAttrSpec attrsSpec;

  public DatasetSpec(String filename, String weightAttrName, String xNodeAttr, String yNodeAttr,
  		String labelAttrName, String areaMapFilename) {
    this(filename, weightAttrName, xNodeAttr, yNodeAttr,
    		labelAttrName, areaMapFilename, Double.NaN);
  }

  public DatasetSpec(String filename, String weightAttrName,
  		String xNodeAttr, String yNodeAttr,
  		String labelAttrName, String areaMapFilename, double valueFilterMin) {
    this(filename, areaMapFilename, new FlowMapAttrSpec(
        weightAttrName,
        labelAttrName,
        xNodeAttr,
        yNodeAttr,
        valueFilterMin
    ));
  }

  private DatasetSpec(String filename, String areaMapFilename, FlowMapAttrSpec attrsSpec) {
    this.filename = filename;
    this.name = FileUtils.getFilenameOnly(filename);
    this.areaMapFilename = areaMapFilename;
    this.attrsSpec = attrsSpec;
  }

  public DatasetSpec withFilename(String filename) {
    return new DatasetSpec(filename, areaMapFilename, attrsSpec);
  }

  public FlowMapAttrSpec getAttrsSpec() {
    return attrsSpec;
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
