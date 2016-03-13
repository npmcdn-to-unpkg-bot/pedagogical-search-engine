package utils

object Logger {
  def info(msg: String) = log("Info", msg)
  def warning(msg: String) = log("Warning", msg)
  def debug(msg: String) = {}//log("Debug", msg)
  def error(msg: String) = log("Error", msg)

  private def log(pre: String, msg: String) = println(s"$pre: $msg")
}
