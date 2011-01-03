package countries.test

import countries._
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class CountriesTest extends FlatSpec with ShouldMatchers {

  Countries.load

  "Countries" should "contain Russia" in {
    Countries.getCountryByCode("RUS").get.code should equal("RUS")
  }
  "Countries" should "contain Switzerland" in {
    Countries.getCountryByCode("CHE").get.name should equal("Switzerland")
  }

}
