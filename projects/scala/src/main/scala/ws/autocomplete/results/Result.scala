package ws.autocomplete.results

trait Result {
  def prettyPrint(): String = toString
  def searchLabel(): String
  def displayLabel(): String
  def pageUri(): String
  def isAvailable(): Boolean

  def isContainedIn(xs: List[Result]): Boolean = xs match {
    case Nil => false
    case head::tail => (head.pageUri().equals(this.pageUri()) || isContainedIn(tail))
  }
}
