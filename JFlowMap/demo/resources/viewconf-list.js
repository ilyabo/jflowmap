var init = function(views) {
  var deployApp = function(viewconf) {
   deployJava.runApplet(
    {id:'jflowmapApplet', code:'jflowmap.JFlowMapApplet', 
    //width:'100%', height:'100%', 
    width:'1', height:'1', 
     archive:'jflowmap.jar'},
    {viewConfig:'viewconf/'+viewconf+'.jfmv', codebase_lookup:'.',
     image:'resources/loading.gif', boxbgcolor:'white', boxborder:'false', centerimage:'true'},
    '1.6');
  };
 var writeViewLinks = function(view) {
  var i, view, viewconf;
  document.write('<h3 class="view">'+view.name+'</h3>');
  if (view.hasOwnProperty('desc')) {
    document.write('<p class="viewdesc">'+view.desc+'</p>');
  }
  document.write('<ul class="views">');
  for (i=0;i<view.viewconfs.length;i++) {    
    viewconf = view.viewconfs[i];
    document.write('<li><a href="?'+viewconf.jfmv+'" target="jflowmapIframe">'+viewconf.name+'</a>');
//    document.write('<li><a href="javascript:deployApp(\''+viewconf.jfmv+'\')">'+viewconf.name+'</a>');
    document.title = viewconf.name;
    if (viewconf.hasOwnProperty('desc')) {
      document.write('<p class="jfvmdesc">&ndash; '+viewconf.desc+'</p>');
    }
  }
  document.write('</ul>');
 };
 var i, view;
 var viewconf = location.search.substr(1);
 if (viewconf) {
  document.write('<table height="100%" width="100%" id="loadingImage"><td align="center" valign="center"><img src="resources/loading.gif"></td></table>');
  /*setTimeout(function() {
    var loading = document.getElementById('loadingImage');
    if (loading != null) {
      if (loading.style) {
        loading.style.display = "none";
      }
    }
  }, 3000);*/
  deployApp(viewconf);
 } else {
  document.write('<div style="margin:10px;"><h1>jflowmap demo applets</h1>');
  for (i=0;i<views.length;i++) { writeViewLinks(views[i]); };
  document.write('</div>');
  document.write('<iframe id="jflowmapIframe" name="jflowmapIframe" src="about:blank" width="100" height="100" style="border-width:0"></iframe>');
 }
};

var jsFlowMap = {
  // These functions are invoked by the applet		
  showSVGCode: function(code) {
    /*
     var width = 800,height = 600;
     var left = parseInt((screen.availWidth/2) - (width/2));
	 var top = parseInt((screen.availHeight/2) - (height/2));
	 var w = window.open('','svgexport',
	  'width='+width+',height='+height+',left='+left+',top='+top
	   +',menubar=0'
	   +',toolbar=0'
	   +',status=0'
	   +',scrollbars=1'
	   +',resizable=1');
	 w.document.writeln(
	  '<html><head><title>Exported SVG</title></head>'
	   +'<body bgcolor="white" onLoad="self.focus()">'+
     '<pre>'+
	   code.replace(/</g,"&lt;").replace(/>/g,"&gt;")+
     //code
	   '</pre>'+
     '</body></html>'
	 );
	 w.document.close();
   */
   parent.parent.document.writeln('<pre>'+
     code.replace(/</g,"&lt;").replace(/>/g,"&gt;")+
     '</pre>');
  },
  viewLoaded: function() {
    // This function is invoked by the applet when the view is loaded
    var loading = document.getElementById('loadingImage');
    if (loading != null) {
      if (loading.style) {
        loading.style.display = "none";
      }
    }
  }
};
