package name.amadoucisse.restoo
package http

import org.scalatest._
import org.scalatest.prop.PropertyChecks

import io.circe.Json
import io.circe.literal._

class JsonPatchSpec extends FunSuite with PropertyChecks with Matchers {

  val fixtures = Table(
    ("op", "input", "expectedOutput"),
    (
      JsonPatch.ReplaceOp("/name", Json.fromString("hello")),
      json"""{"name":"not hello"}""",
      json"""{"name":"hello"}"""
    ),
    (
      JsonPatch.ReplaceOp("/not_name", Json.fromString("not hello")),
      json"""{"name":"hello"}""",
      json"""{"name":"hello"}"""
    ),
  )

  test("parse replace op") {

    JsonPatch.fromJson(json"""{"op":"replace","path":"/priceInCents","value":9999}""") shouldEqual Vector(
      JsonPatch
        .ReplaceOp(path = "/priceInCents", value = Json.fromInt(9999))
    )
  }

  test("applyOperation") {

    forAll(fixtures) { (op, input, expectedOutput) ⇒
      op.applyOperation(input) shouldEqual expectedOutput
    }
  }

}
