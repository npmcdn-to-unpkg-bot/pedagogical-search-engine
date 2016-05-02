package ws.indices.enums

object WebsiteSourceType
extends Enumeration {
  type WebsiteSource = Value

  val Khan = Value("khan")
  val Mit = Value("mit")
  val Scholarpedia = Value("scholarpedia")
  val Safari = Value("safari")
  val Coursera = Value("coursera")
  val Web = Value("web")

  def toPublicString(source: WebsiteSource) = source match {
    case Khan => "Khan"
    case Mit => "MIT"
    case Scholarpedia => "Scholarpedia"
    case Safari => "Book"
    case Coursera => "Coursera"
    case Web => "Web"
  }

  def fromWcTypeField(field: String): WebsiteSource = field match {
    case "Khan Academy" => Khan
    case "Book" => Safari
    case "MIT" => Mit
    case "Coursera" => Coursera
    case "Scholarpedia" => Scholarpedia
    case _ => Web
  }
}
