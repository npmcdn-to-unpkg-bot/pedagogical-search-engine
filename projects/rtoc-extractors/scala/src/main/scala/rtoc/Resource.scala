package rtoc

import java.io.{File, PrintWriter}

import Utils.Logger
import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

class Resource(nodes: List[Node], metadata: JValue,
               folder: String, name: String) {
  def write(): Unit = {
    Logger.info(s"Writing resource")

    // Creating the json
    val c = nodes.map(_.json())
    val o: JObject = ("children" -> c) ~ ("metadata" -> metadata)
    val json = pretty(render(o))

    // (Re)-Create folder
    new File(folder).mkdirs()

    // Create file
    val file = new File(s"$folder/$name.rtoc")
    file.createNewFile()

    // Writing it
    val pw = new PrintWriter(file)
    pw.write(json)
    pw.close()
  }
}
