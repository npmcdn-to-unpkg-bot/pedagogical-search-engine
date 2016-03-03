package rtoc

import java.io.{PrintWriter, File}

import Utils.Logger
import org.json4s.JsonAST.JObject
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

class Resource(nodes: List[Node], path: File) {
  def write(): Unit = {
    Logger.info(s"Writing resource")

    // Creating the json
    val c = nodes.map(_.json())
    val o: JObject = ("children" -> c)
    val json = pretty(render(o))

    // Writing it
    val pw = new PrintWriter(path)
    pw.write(json)
    pw.close()
  }
}
