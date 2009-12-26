package jflowmap;

public class FlowMapAttrsSpec {
    public final String weightAttrName;
    public final String labelAttrName;
    public final String xNodeAttr, yNodeAttr;
    public final double weightFilterMin;
    public FlowMapAttrsSpec(String weightAttrName, String labelAttrName,
            String xNodeAttr, String yNodeAttr, double weightFilterMin) {
        this.weightAttrName = weightAttrName;
        this.labelAttrName = labelAttrName;
        this.xNodeAttr = xNodeAttr;
        this.yNodeAttr = yNodeAttr;
        this.weightFilterMin = weightFilterMin;
    }
    public String getWeightAttrName() {
        return weightAttrName;
    }
    public String getLabelAttrName() {
        return labelAttrName;
    }
    public String getXNodeAttr() {
        return xNodeAttr;
    }
    public String getYNodeAttr() {
        return yNodeAttr;
    }
    public double getWeightFilterMin() {
        return weightFilterMin;
    }
}