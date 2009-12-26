package jflowmap;


public class DatasetSpec {
    public DatasetSpec(String filename, String valueAttrName, String xNodeAttr, String yNodeAttr,
    		String labelAttrName, String areaMapFilename) {
        this(filename, valueAttrName, xNodeAttr, yNodeAttr,
        		labelAttrName, areaMapFilename, Double.NaN);
    }

    public FlowMapAttrsSpec getAttrsSpec() {
        return attrsSpec;
    }

    public DatasetSpec(String filename, String weightAttrName,
    		String xNodeAttr, String yNodeAttr,
    		String labelAttrName, String areaMapFilename, double valueFilterMin) {
        this.filename = filename;
        this.areaMapFilename = areaMapFilename;
        this.attrsSpec = new FlowMapAttrsSpec(
                weightAttrName,
                labelAttrName,
                xNodeAttr,
                yNodeAttr,
                valueFilterMin
        );
    }
    public final String filename;
    public final String areaMapFilename;
    public final FlowMapAttrsSpec attrsSpec;

    @Override
    public String toString() {
        return filename;
    }
}