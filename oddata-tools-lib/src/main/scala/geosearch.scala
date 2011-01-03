package geosearch

import java.net.URLEncoder
import java.net.URL
import java.io.{InputStreamReader, BufferedReader}
import xml._

case class GeoEntity(name:String, lat:BigDecimal, lon:BigDecimal)

/**
 * Provides methods for querying
 * the <a href="http://www.geonames.org/export/geonames-search.html">GeoNames</a> web service
 * in order to find the detailed information (including the lat/lon coordinates)
 * for a specific geographic location by name/country/class.
 */
object GeoSearch {

  /**
   * @param featureCode Check <a href="http://www.geonames.org/export/codes.html">feature codes and classes</a>.
   */
  def find(query:String, countryCode:String, featureClass:String, featureCode:String):Option[GeoEntity] = {
    val coords = getCoords(query, countryCode, featureClass, featureCode)
    if (coords == null) {
      println("Warn: coords for " + query + " not found")
      None
    } else {
      Some(new GeoEntity(coords._1, coords._2, coords._3))
    }
  }

  private def getCoords(query:String, countryCode:String, featureClass:String, featureCode:String):(String, BigDecimal, BigDecimal) = {
    val url = "http://ws.geonames.org/search?q=" + URLEncoder.encode(query, "utf-8") +
            (if (countryCode != null) "&country=" + countryCode else "") +
            (if (featureClass != null) "&featureClass=" + featureClass else "") +
            (if (featureCode != null) "&featureCode=" + featureCode else "")
    val geonames = XML.loadString(readUrl(url))

    if ((geonames \ "geoname").size  > 0) {
      var firstGeoname = (geonames \ "geoname").head
      //    println("Warning: " + numOfResults + " results for '" + query + "'. First result name: " + (firstGeoname \ "name").head.text)
        ((firstGeoname \ "name").head.text, // + ", " + (firstGeoname \ "countryCode").head.text,
         BigDecimal((firstGeoname \ "lat").head.text),
         BigDecimal((firstGeoname \ "lng").head.text))
    } else {
      null
    }
  }

  private def readUrl(url:String) = {
    val in = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "utf-8"))
    val response = new StringBuilder
    var inputLine:String = null
    do {
      inputLine = in.readLine
      if (inputLine != null) response.append(inputLine)
    } while (inputLine != null)
    in.close

    response.toString
  }
}