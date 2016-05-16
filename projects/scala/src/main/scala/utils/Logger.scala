package utils

import java.io.{PrintWriter, StringWriter}

object Logger {
  def info(msg: String) = log("Info", msg)
  def warning(msg: String) = log("Warning", msg)
  def debug(msg: String) = {}//log("Debug", msg)
  def error(msg: String) = log("Error", msg)
  def stackTrace(msg: String, e: Throwable) = {
    val errors = new StringWriter()
    e.printStackTrace(new PrintWriter(errors))
    log("StackTrace", msg)
    log("StackTrace", errors.toString)
  }

  private def log(pre: String, msg: String) = println(s"$pre: $msg")
}
