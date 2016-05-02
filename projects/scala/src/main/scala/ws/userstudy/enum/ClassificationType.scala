package ws.userstudy.enum

object ClassificationType
extends Enumeration{
  type Classification = Value

  val relevant = Value("relevant")
  val irrelevant = Value("irrelevant")

  def fromString(label: String)
  : Option[Classification] = {
    if(label.equals(relevant.toString)) {
      Some(relevant)
    } else if(label.equals(irrelevant.toString)) {
      Some(irrelevant)
    } else {
      None
    }
  }
}
