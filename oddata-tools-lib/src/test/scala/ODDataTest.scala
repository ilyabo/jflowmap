package oddata.test

import oddata.{OriginDest, Node, NodeInMapSearch, ODData}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class ODDataTest extends FlatSpec with ShouldMatchers {

  it should "load nodes and flows from two CSV strings" in {

    val nodes = ODData.loadNodesFromCsv(
      delim = ',', idCol = 0, nameCol = 1, latCol = 2, lonCol = 3,
      reader = ODData.stringReader(
"""Code,Name,Lat,Lon
A,Node A,10,10
B,Node B,11,10
C,Node C,12,10.5
"""))

    val data = new ODData(NodeInMapSearch(nodes))
    data.loadFlowlistFromCsv(
      delim = ',', originCol = 0, destCol = 1, attrCols = List(2,3,4,5),
      reader = ODData.stringReader(
"""Origin,Dest,1999,2000,2001,2002
A,B,1,2,1,0
B,A,2,1,1,1
A,C,2,5,4,1
B,C,3,2,3,1
"""))

    data.getNodes.length should equal(3)

    val a = new Node("A", "Node A", Some(10), Some(10))
    val b = new Node("B", "Node B", Some(11), Some(10))
    val c = new Node("C", "Node C", Some(12), Some(10.5))

    data.getNodes should contain(a)
    data.getNodes should contain(b)
    data.getNodes should contain(c)


    data.getFlowAttrs should equal(List("1999", "2000", "2001", "2002"))

    data.getFlowsByOriginDest.values.size should equal(4)

    data.getFlowsByOriginDest.get(new OriginDest(a, b)).get.size should equal(4)


    data.getFlow(a, b, "1999").get.value.get should equal(1)
    data.getFlow(b, c, "2001").get.value.get should equal(3)
  }

}
