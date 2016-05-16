package rsc.attributes

object Source extends Enumeration {
  type Source = Value

  val courseraStr = "coursera"
  val khanStr = "khan"
  val mitStr = "mit"
  val safariStr = "safari"
  val scholarpedia = "scholarpedia"

  val Coursera = Value(courseraStr)
  val Khan = Value(khanStr)
  val MIT = Value(mitStr)
  val Safari = Value(safariStr)
  val Scholarpedia = Value(scholarpedia)

  def fromString(s: String)
  : Source = s match {
    case `courseraStr` => Coursera
    case `khanStr` => Khan
    case `mitStr` => MIT
    case `safariStr` => Safari
    case `scholarpedia` => Scholarpedia
  }

  def fromUrl(url: String)
  : Source = url match {
    case x if url.contains("coursera.org") => Coursera
    case x if url.contains("mit.edu") => MIT
    case x if url.contains("safaribooksonline.com") => Safari
    case x if url.contains("scholarpedia.org") => Scholarpedia
    case x if url.contains("khanacademy.org") => Khan
  }
}
