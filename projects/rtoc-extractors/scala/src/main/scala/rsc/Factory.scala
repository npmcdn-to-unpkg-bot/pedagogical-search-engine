package rsc

abstract class Factory[U <: HasStatus] {

  // Either override this method, or use the
  // default implementation
  def getResource(data: U): Option[Resource] = javaStyle(data, getOrFail)

  // .. but then you should implement this
  @throws(classOf[Exception])
  protected def getOrFail(data: U): Resource = throw new Exception("You should implement this method.")

  // Internal things
  private def javaStyle(data: U, fn: U => Resource): Option[Resource] = {
    try {
      Some(fn(data))
    } catch {
      case e => None
    }
  }
}
