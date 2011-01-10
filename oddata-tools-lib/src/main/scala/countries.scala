package countries

import collection.mutable.ListBuffer
import xml.XML
import com.Ostermiller.util.CSVParser
import java.io.InputStreamReader
import scala.collection._


case class Country(id:Int, code:String, name:String, name2:String,
                   lat:BigDecimal, lon:BigDecimal, region:Option[Region],
        population:Option[ListBuffer[Population]]) {
  def shorterName = {
    if (name.length < name2.length)
      name
    else
      name2
  }
}

case class Region(id:Int, name:String, parent:Option[Region])

case class Population(year:Int, value:Int)

object Countries {

  val countriesPath = "/countries-data/"
  val regions = new ListBuffer[Region]
  val countryCodeToRegion = mutable.Map.empty[String, Region]

  val countryNameToCode = mutable.Map.empty[String, String]
  val countryCodeToName = mutable.Map.empty[String, String]
  val countriesByCode = mutable.Map.empty[String, Country]
  val populationByCountryName = mutable.Map.empty[String, ListBuffer[Population]]

  def load() {
    println("Loading countries")
    loadRegions(countriesPath + "regions.xml")
    loadPopulation(countriesPath + "population.csv")

    val countriesXml = XML.load(resourceURL(countriesPath + "country-names.xml"))
    for (country <- countriesXml \ "country") {
      val name = (country \ "@name").head.text
      val code = (country \ "@code").head.text
      countryCodeToName(code) = name
      countryNameToCode(name) = code
    }

    val countriesLatLonXml = XML.load(resourceURL(countriesPath + "country-nodes.xml"))
    for (node <- countriesLatLonXml \ "node") {
      val code = (node \ "@code").head.text
      val name2 = (node \ "@name").head.text
      val name = countryCodeToName.getOrElse(code, name2)
      val population = (if (populationByCountryName.contains(name)) Some(populationByCountryName(name)) else populationByCountryName.get(name2))
      val country = new Country(
                countriesByCode.size + 1, code, name, name2,
                BigDecimal((node \ "@lat").head.text), BigDecimal((node \ "@lon").head.text),
                countryCodeToRegion.get(code),
                if (population.isDefined)
                  Some(population.get.sortWith{_.year < _.year})
                else None)
//      if (!countryCodeToRegion.contains(code)) println("Warn: No region for country " + country)
//      if (!populationByCountryName.contains(name)) println("Warn: No population records for country " + country)
      countriesByCode(code) = country
    }

    for ((name,code) <- countryNameToCode) {
      if (!countriesByCode.contains(code)) {
        println("Warn: country " + code + ":" + name + " is not in countriesByCode")
      }
    }

  }

  def resourceURL(path: String) = this.getClass.getResource(path)

  def resourcePath(path: String) = {
    val res = resourceURL(path)
    if (res != null) res.toString else path
  }


  def loadRegions(fname: String) {
    println("Loading regions")
    val regionsXml = XML.load(resourceURL(fname))
    for (region <- regionsXml \ "region") {
      parseRegion(region, None)
    }
  }

  def loadPopulation(fname: String) {
    println("Loading population")
    val csvLines = CSVParser.parse(new InputStreamReader(resourceURL(fname).openStream, "utf-8"), ';', "nrtf", "\n\r\t\f", "#!")
    for (i <- 1 to csvLines.length - 1) {
      val line = csvLines(i)
      val countryName = line(1)
      val year = line(2).toInt
      val value = (line(4).toDouble*1000).toInt
      val list = populationByCountryName.getOrElseUpdate(countryName, new ListBuffer[Population])
      list += new Population(year, value)
    }
  }

  def parseRegion(regionNode:xml.Node, parent:Option[Region]) {
    val region = new Region(regions.size + 1, (regionNode \ "@name").head.text, parent)
    regions += region
    for (r <- regionNode \ "region") {
      parseRegion(r, Some(region))
    }
    for (country <- regionNode \ "country") {
      countryCodeToRegion((country \ "@iso-code").head.text) = region
    }
  }

  def getPopulationEstimate(country:Country, year:Int):Option[Int] = {
    if (country.population.isDefined) {
      var prev:Option[Population] = None
      for (p <- country.population.get) {
        if (p.year == year)
          return Some(p.value)
        else {
          if (p.year > year) {
            if (!prev.isDefined) return None
            // linearly interpolate
            val prevv = prev.get.value
            val prevy = prev.get.year
            val nextv = p.value
            val nexty = p.year
            val diff:Double = (nextv - prevv) * ((year.toDouble - prevy) / (nexty - prevy))
            return Some(prevv + diff.round.toInt)
          }
        }
        prev = Some(p)
      }
    }
    return None
  }

  def getCountryByCode(code:String) =
    if (warnIfCountryCodeIsUnknown(code))
      countriesByCode.get(code)
    else
      None

  def getCountryByName(name:String) =
    if (warnIfCountryIsUnknown(name))
      getCountryByCode(countryNameToCode(name))
    else
      None

  def warnIfCountryCodeIsUnknown(code:String) = {
    if (!countriesByCode.contains(code)) {
      println("Warn: Country with code '" + code + "' not found")
      false
    } else {
      true
    }
  }

  def warnIfCountryIsUnknown(countryName:String) = {
    if (!countryNameToCode.contains(countryName)) {
      println("Warn: Country " + countryName + " not found")
      false
    } else {
      true
    }
  }

}
