package ws.exploration.attributes

import ws.exploration.attributes.QuestionIdType.QuestionId

case class Feedback(questionId: QuestionId,
                    value: String) {

}
