package ws.exploration.attributes

object CategoryType
extends Enumeration {
  type Category = Value

  val Feedback = Value("feedback")
  val ForgetUser = Value("forget-user")
  val UnforgetUser = Value("unforget-user")
}
