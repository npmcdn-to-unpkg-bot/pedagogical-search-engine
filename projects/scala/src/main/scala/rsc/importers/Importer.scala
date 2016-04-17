package rsc.importers

object Importer extends Enumeration {
  type Importer = Value

  val SlickMysql = Value("slick-mysql")
}
