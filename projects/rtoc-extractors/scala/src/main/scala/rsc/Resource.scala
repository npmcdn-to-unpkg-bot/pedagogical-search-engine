package rsc

import java.io.{File, PrintWriter}

import org.json4s.JsonDSL._
import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.native.JsonMethods._
import rsc.Types.{Metadata, TOCs}

class Resource(oMetadata: Option[Metadata],
               oTocs: Option[TOCs],
               oDescriptions: Option[List[String]],
               folder: String, name: String) {
  def write(): Unit = {
    // (tocs) element
    val tcoJSON = oTocs match {
      case None => None
      case Some(xs) => Some(xs.map(toc => toc.nodes.map(node => node.json())))
    }

    val o: JObject = ("metadata" -> oMetadata) ~ ("tocs" -> tcoJSON) ~ ("descriptions" -> oDescriptions)
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
