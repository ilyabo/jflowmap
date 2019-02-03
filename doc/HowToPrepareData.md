_For a full list of the view config properties refer to [ViewConfig properties list](./ViewConfigProperties.md)._


## Minimal configuration example ##

To load a dataset in a specific view (flowmap or flowstrates) you need to create a view configuration file (a .jfmv file) which you can then use to open the visualization both in the desktop app version or as an applet.

So we'll need two [CSV files](http://en.wikipedia.org/wiki/Comma-separated_values) with the data: nodes.csv and flows.csv, a shapefile with the map, and a config file flowmap-config.jfmv.

Here's the content of nodes.csv:
```
Code,Name,Lat,Lon
SDN,Sudan,13.197363,30.439371
ARE,Emirates,24.154436,54.363366
DEU,Germany,51.77387,10.574742
ECU,Ecuador,-1.147179,-78.247751
UGA,Uganda,1.324938,32.233743
GBR,United Kingdom,52.597352,-1.534873
IRN,Iran,32.673635,53.738655
JPN,Japan,35.992619,137.776432
```


Here's flows.csv in which we refer to the nodes by their "Code":
```
Origin,Dest,Refugees in 2009
SDN,ARE,15
DEU,ECU,1
UGA,GBR,1601
IRN,JPN,31
```

And here is the config file which puts it all together:

```
view=flowmap

data=csv
data.csv.nodes.src=nodes.csv
data.csv.flows.src=flows.csv


data.attrs.node.id=Code
data.attrs.node.label=Name
data.attrs.node.lat=Lat
data.attrs.node.lon=Lon
data.attrs.flow.origin=Origin
data.attrs.flow.dest=Dest

data.attrs.flow.weight.csvList=Refugees in 2009


map=shapefile
map.shapefile.src=countries.shp
map.projection=Mercator
```


If you don't have a shapefile with a suitable map, you can simply omit the properties starting with "map.shapefile".


_Here you can find  [all the files for this minimal JFlowMap config example](../demo/minimal-config-example/) to work._






## More sophisticated example ##
Here we have more flow weight attributes which represent changes over time of the flow magnitudes:

```
view=flowstrates

data=csv
data.csv.separator=,
data.csv.nodes.src=data/refugee-nodes.csv
data.csv.flows.src=data/refugee-flows.csv

data.attrs.node.id=Code
data.attrs.node.label=Name
data.attrs.node.lat=Lat
data.attrs.node.lon=Lon
data.attrs.flow.origin=Origin
data.attrs.flow.dest=Dest
data.attrs.flow.weight.csvList= 1975,1976,1977,1978,1979,\
       1980,1981,1982,1983,1984,1985,1986,1987,1988,1989,\
       1990,1991,1992,1993,1994,1995,1996,1997,1998,1999,\
       2000,2001,2002,2003,2004,2005,2006,2007,2008,2009

map=shapefile
map.shapefile.src=data/shapefiles/world.shp
map.shapefile.dbf.areaIdField=ISO_A3
map.projection=Mercator
```

The most important thing here is specifying the data attributes: i.e. which columns of the CSV files represent the node ids, labels, coordinates and the flow weights.

The nodes (countries, in this case) are in 'data/refugee-nodes.csv' (if your data is about world countries, you can use [this file](http://code.google.com/p/jflowmap/source/browse/trunk/JFlowMap/demo/data/refugee-nodes.csv.gz) for the nodes), the flows - in 'data/refugee-flows.csv'. Here is how the nodes CSV file is structured:

```
Code,Name,Lat,Lon
LCA,Saint Lucia,13.903085,-60.9659
BRN,Brunei Darussalam,4.581283,114.819152
MDG,Madagascar,-18.054455,47.108621
KNA,Saint Kitts and Nevis,17.313103,-62.736679
UZB,Uzbekistan,41.447353,64.79929
LSO,Lesotho,-29.595733,28.244114
SLB,Solomon Islands,-8.910545,159.537743
MDV,Maldives,3.353159,73.260862
...
```

and the flows CSV:
```
Origin,Dest,1975,1976,1977,1978,1979,1980,1981,1982,1983,1984,1985,1986,1987,1988,1989,1990,1991,1992,1993,1994,1995,1996,1997,1998,1999,2000,2001,2002,2003,2004,2005,2006,2007,2008,2009
SEN,ESP,,,,,,,,,,,,,,,,,,,1,1,,2,2,3,4,4,3,3,3,3,2,2,2,2,2
CHN,PER,,,,,,,,,,,,,,,,,,,,,,,1,,1,1,1,1,1,2,2,2,2,2,2
CHL,MOZ,,,,,,,,,300,250,190,140,130,130,130,160,160,140,,,,,,,,,,,,,,,,,
ECU,DEU,,,,,,,,,,,,,,,,,,,,,,,,,,,,86,100,98,91,73,160,154,127
SRB,ISR,,,,,,,,,,,,,,,,,,,,,,,,,1,,,,,,,,,,
IRQ,HUN,,,,,,,,,,,,,,,,,,,11,17,,,,,,201,258,700,938,1294,1252,1235,1272,1243,1166
URY,ARG,570,1050,,,,,,,,,,,,,,,,,,,,,,,,,,,,1,1,1,1,,
...
```

The **data.attrs.node.id** property specifies the column of the nodes CSV file by the values of which the nodes are identified. To let JFlowMap find the nodes corresponding to the flows' origins and destinations  these node IDs must be used as the values of the Origin and Dest columns of the flows CSV file.

The **map.projection** property can be currently set to one of the following values: None, FlipY, Mercator, and WinkelTripel.

If your shapefile is supplied with a .dbf file in which there is a field with the IDs of the geometries in the map, then you should specify the name of this field in the **map.shapefile.dbf.areaIdField** property in the .jfmv file. This way JFlowMap will be able to find in the shapefile the polygons corresponding to the nodes specified in the CSV file and colorize them, e.g. showing the total flow magnitudes when you select a column in the heatmap.


As soon as you have a .jfmv file you can run jflowmap as a desktop application:
```
java -jar jflowmap.jar my.jfmv
```

or deploy it as an applet:
```
<html>
<head>
<script type="text/javascript" src="http://www.java.com/js/deployJava.js"></script>
<script type="text/javascript">
var deployApp = function(viewconf) {
 deployJava.runApplet(
  {code:'jflowmap.JFlowMapApplet', width:'100%', height:'100%', archive:'jflowmap.jar'},
  {viewConfig:viewconf, codebase_lookup:'.'}, '1.6');
};
</script>
</head>
<body style="overflow:hidden; padding:0px 0px; margin:0px 0px">
  <script type="text/javascript">deployApp('my.jfmv');</script>
</body>
</html>
```

You can find more examples of the config files in the 'viewconf' folder of the demo package.
