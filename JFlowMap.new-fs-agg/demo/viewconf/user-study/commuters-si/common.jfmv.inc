view.flowmap.colorScheme=Light Blue

view.flowmap.fillEdgesWithGradient=false
view.flowmap.showDirectionMarkers=false
view.flowmap.showNodes=false
view.flowmap.edgeWidth=15
view.flowmap.edgeOpacity=150
view.flowmap.showSelfLoops=true
view.flowmap.showFlowWeightAttrLabel=true

#view.flowmap.weightFilterMin=50


data=graphml
data.graphml.src=../../../data/commuters-si.xml.gz

# remove self-loops
data.select.flows.where=([source] != [target])  
# NOT ISNAN is necessary, because Double.compareTo treats NaNs greater than everything else
data.select.flows.where.exists=((NOT ISNAN([#weightAttr#])) AND ([#weightAttr#] > 100))


data.attrs.node.label=name
data.attrs.node.lat=lat
data.attrs.node.lon=lon
data.attrs.flow.weight.csvList=2000,2001,2002,2003,2004,2005,2006,2007,2008


map.projection=Mercator


map.background.src=../../../data/maps/slo/slo-map.png
map.background.boundingBox=7.303700109638609,-29.761913892631096,2.12921263064913,1.5416999478281372
map.background.transparency=0.5

window.settings.embed=true