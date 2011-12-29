view.flowmap.colorScheme=Dark
view.flowmap.fillEdgesWithGradient=false
view.flowmap.showDirectionMarkers=false
view.flowmap.showNodes=false
view.flowmap.edgeWidth=10
view.flowmap.edgeOpacity=200
view.flowmap.showSelfLoops=false
view.flowmap.showFlowWeightAttrLabel=true


#view.flowmap.weightFilterMin=1000


data=csv
data.csv.separator=,
data.csv.charset=utf-8
data.csv.nodes.src=../../../data/refugee-nodes.csv.gz
data.csv.flows.src=../../../data/refugee-flows.csv.gz

data.select.flows.where=([source]!=[target])
data.select.flows.where.exists=((NOT ISNAN([#weightAttr#])) AND ([#weightAttr#] > 600))


data.attrs.node.id=Code
data.attrs.node.label=Name
data.attrs.node.lat=Lat
data.attrs.node.lon=Lon
data.attrs.flow.origin=Origin
data.attrs.flow.dest=Dest
#data.attrs.flow.weight.re=[0-9]{4}
data.attrs.flow.weight.csvList=1975,1976,1977,1978,1979,\
                               1980,1981,1982,1983,1984,1985,1986,1987,1988,1989,\
                               1990,1991,1992,1993,1994,1995,1996,1997,1998,1999,\
                               2000,2001,2002,2003,2004,2005,2006,2007,2008,2009


map.projection=WinkelTripel

map.background.src=../../../data/maps/countries/countries.png
map.background.boundingBox=-2.722533516968113,-1.8260025298264655,5.601597416002858,3.4280609186475823
map.background.transparency=0.5

window.settings.embed=true
window.title=Refugees
