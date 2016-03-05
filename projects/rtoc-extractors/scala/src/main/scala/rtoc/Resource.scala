package rtoc

import java.io.{File, PrintWriter}

import Utils.Logger
import org.json4s.JsonDSL._
import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.native.JsonMethods._
import rtoc.Types.{Syllabuses, Nodes}

class Resource(syllabuses: Syllabuses, metadata: JValue,
               folder: String, name: String) {
  def write(): Unit = {
    // Creating the json
    val c = syllabuses.map(syllabus => syllabus.nodes.map(_.json()))
    val o: JObject = ("children" -> c) ~ ("metadata" -> metadata)
    val json = pretty(render(o))

    // (Re)-Create folder
    new File(folder).mkdirs()

    // Create file
    val file = new File(s"$folder/$name.rsc")
    file.createNewFile()

    // Writing it
    val pw = new PrintWriter(file)
    pw.write(json)
    pw.close()
  }
}
