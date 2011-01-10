package geosearch.test

import geosearch.GeoSearch
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers


class GeoSearchTest extends FlatSpec with ShouldMatchers {

  "GeoSearch" should "find canton Fribourg by name" in {
    GeoSearch.find("Fribourg", "CH", null, "ADM1").get.name should equal("Canton de Fribourg")
  }
  "GeoSearch" should "find Tribeca by name" in {
    GeoSearch.find("Tribeca", "US", null, "PPLX").get.name should equal("TriBeCa")
  }

}
