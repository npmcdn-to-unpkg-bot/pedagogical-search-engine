package ws.userstudy.enum

object ClassificationType
extends Enumeration{
  type Classification = Value

  val relevant = Value("relevant")
  val irrelevant = Value("irrelevant")
  val relevantPatch = Value("rlvpatch")
  val irrelevantPatch = Value("irlvpatch")
}
