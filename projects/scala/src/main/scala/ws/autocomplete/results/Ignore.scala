package ws.autocomplete.results

case class Ignore() extends Result {
  override def searchLabel(): String = throw new NotImplementedError()
  override def pageUri(): String = throw new NotImplementedError()
  override def displayLabel(): String = throw new NotImplementedError()
  override def isAvailable(): Boolean = throw new NotImplementedError()
}
