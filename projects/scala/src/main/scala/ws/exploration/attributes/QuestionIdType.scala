package ws.exploration.attributes

object QuestionIdType
extends Enumeration {
  type QuestionId = Value

  val Q1 = Value("Q1")
  val Q2 = Value("Q2")
  val Q3 = Value("Q3")
  val Q4InGeneral = Value("Q4-ingeneral")
  val Q4Entry = Value("Q4-entry")
  val textFeedback = Value("textfeedback")
}
