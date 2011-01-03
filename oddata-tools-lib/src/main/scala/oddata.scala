package oddata

import com.Ostermiller.util.CSVParser
import geosearch.GeoSearch
import java.io.{StringReader, Reader, FileInputStream, InputStreamReader}
import scala.collection._
import mutable.ListBuffer
import countries.{Countries, Country}

case class OriginDest(origin:Node, dest:Node)

case class Flow(originDest:OriginDest, attr:String, value:Option[BigDecimal])

case class Node(code:String, name:String, lat:Option[BigDecimal], lon:Option[BigDecimal])



trait NodeSearch {
  def getNode(code:String):Option[Node]
}

object NodeInMapSearch {
  def apply(map:Map[String,Node]) = new NodeSearch {
    def getNode(code:String) = map.get(code)
  }
}

object NodeGeoSearch {
  def apply(countryCode:String, featureClass:String, featureCode:String) = new NodeSearch {
    def getNode(code:String) = GeoSearch.find(code, countryCode, featureClass, featureCode) match {
      case Some(entity) => Some(new Node(code, entity.name, Some(entity.lat), Some(entity.lon)))
      case _ => None
    }
  }
}

object NodeCountrySearch {
  def byName = new NodeSearch {
    def getNode(name:String) = node(name, Countries.getCountryByName(name))
  }
  def byCode = new NodeSearch {
    def getNode(code:String) = node(code, Countries.getCountryByCode(code))
  }
  private def node(nameOrCode:String, country:Option[Country]) = country match {
    case Some(country) => Some(new Node(country.code, country.name, Some(country.lat), Some(country.lon)))
    case _ => None
  }
}



object ODData {
  def fileReader(fileName:String, encoding:String = "utf-8") = new InputStreamReader(new FileInputStream(fileName), encoding)
  def stringReader(text:String) = new StringReader(text)
  trait CsvLineAttrNameResolver {
    def apply(header:Seq[String], line:Seq[String], attrCol:Int):String
  }

  def loadNodesFromCsv(reader:Reader, delim:Char, idCol:Int, nameCol:Int, latCol:Int, lonCol:Int) = {
    val nodeMap = mutable.Map.empty[String, Node]
    val lines = CSVParser.parse(reader, delim, "nrtf", "\n\r\t\f", "#!")
    val header = lines(0)
    for (li <- 1 until lines.length) {
      val line = lines(li)
      nodeMap(line(idCol)) = new Node(line(idCol), line(nameCol), parseBigDecimal(line(latCol), li), parseBigDecimal(line(lonCol), li))
    }
    nodeMap
  }

  def parseBigDecimal(str:String, line:Int = -1) =
    try {
      if (str.trim.length == 0)
        None  // no need to print the warning message
      else
        Some(BigDecimal(str))
    } catch {
      case nfe:NumberFormatException =>
        printf("Warn: Cannot parse number '%s' in line %d. Adding a None-value flow.\n", str, line); None
    }

}


class ODData(nodeFactory:NodeSearch) {

  private val nodesByCode = mutable.Map.empty[String, Node]
  private val flowAttrs = mutable.LinkedHashSet.empty[String]
  private val flowsByOriginDest = mutable.LinkedHashMap.empty[OriginDest, ListBuffer[Flow]]

  def getNodes =
    List[Node]() ++ nodesByCode.values  // convert to immutable

  def getNodesByCode =
    Map[String, Node]() ++ nodesByCode  // convert to immutable

  def getFlowAttrs =
    List[String]() ++ flowAttrs

  def getFlowsByOriginDest =
    Map[OriginDest, Seq[Flow]]() ++ flowsByOriginDest  // convert to immutable map

  def getFlow(origin:Node, dest:Node, attr:String) = {
    val flows = flowsByOriginDest.get(new OriginDest(origin, dest))
    if (flows.isDefined)
      flows.get.find(_.attr.equals(attr))
    else
      None
  }

  val defaultAttrNameResolver = new ODData.CsvLineAttrNameResolver {
    def apply(header:Seq[String], line:Seq[String], attrCol:Int):String = header(attrCol)
  }

  def loadFlowlistFromCsv(reader:Reader, delim:Char, originCol:Int, destCol:Int, attrCols:Seq[Int],
                          attrNames:ODData.CsvLineAttrNameResolver = defaultAttrNameResolver) {
    val lines = CSVParser.parse(reader, delim, "nrtf", "\n\r\t\f", "#!")
    val header = lines(0)
    for (li <- 1 until lines.length) {
      val line = lines(li)
      for (attrCol <- attrCols) {
        addFlow(getNode(line(originCol)), getNode(line(destCol)), attrNames(header, line, attrCol),
          ODData.parseBigDecimal(line(attrCol), li))
      }
    }
  }

  def loadODMatrixCsv(reader:Reader, flowAttr:Option[String], delim:Char) {
    printf("Loading OD matrix ")
    val lines = CSVParser.parse(reader, delim, "nrtf", "\n\r\t\f", "#!")
    var li = 0
    var flowAttrName = flowAttr.getOrElse("unknown")
    if (lines(0).length == 1) {
      // if no attr provided and the first line is a caption, use it
      if (flowAttr.isEmpty) flowAttrName = lines(0)(0)
      li = li + 1  // skip this line
    }
    printf("for flow attribute '%s'\n", flowAttrName)
    if (lines.length > li) {
      val header = lines(li)
      for (i <- (li + 1) until lines.length) {
        val line = lines(i)
        val origin = getNode(line(0))
        for (j <- 1 to line.length - 1) {
          val dest = getNode(header(j))
          val flow = addFlow(origin, dest, flowAttrName, ODData.parseBigDecimal(line(j), j+1))
        }
      }
    } else {
      printf("Warn: empty matrix for attr '%s'\n", flowAttrName)
    }
  }

  private def addFlow(origin:Node, dest:Node, attr:String, value:Option[BigDecimal]) = {
    val od = new OriginDest(origin, dest)
    val flow = new Flow(od, attr, value)
//    println("addFlow: " + flow)
    if (!flowsByOriginDest.contains(od)) {
      flowsByOriginDest.put(od, mutable.ListBuffer.empty[Flow])
    }
    flowsByOriginDest(od) += flow
    flowAttrs += flow.attr
  }

  private var numNodesNotFoundByFactory = 0

  def getNumNodesNotFound = {
    numNodesNotFoundByFactory
  }

  private def getNode(code:String) =
    if (nodesByCode.contains(code)) {
      nodesByCode(code)
    } else {
      val nodeOption = nodeFactory.getNode(code)
      if (nodeOption.isEmpty) {
        numNodesNotFoundByFactory += 1
      }
      val node = nodeOption.getOrElse(new Node(code, code, None, None))
      nodesByCode(code) = node
      node
    }

}
