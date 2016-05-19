package ws.userstudy.enum

object ClassificationType
extends Enumeration{
  type Classification = Value

  val relevant = Value("relevant")
  val rlvpatch = Value("rlvpatch")
  val rlvunselect = Value("rlvunselect")

  val irrelevant = Value("irrelevant")
  val irlvpatch = Value("irlvpatch")
  val irlvunselect = Value("irlvunselect")
}
