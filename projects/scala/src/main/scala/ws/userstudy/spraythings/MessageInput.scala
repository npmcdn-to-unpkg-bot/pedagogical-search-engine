package ws.userstudy.spraythings

case class MessageInput(category: String,
                        content: String,
                        sid: Option[Int]) {
}
