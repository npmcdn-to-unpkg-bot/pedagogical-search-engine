package rsc.importers

import rsc.Resource
import rsc.importers.Importer.Importer
import rsc.importers.Importer.SlickMysql

import scala.concurrent.{ExecutionContext, Future}

class SlickMysql(_ec: ExecutionContext) {

  implicit val ec: ExecutionContext = _ec

  def importResource(r: Resource)
  : Future[Resource] = {
    Future {
      // todo: Import the resource

      // Write in the resource that we imported it
      val existingImporters = r.oImporters match {
        case None => Nil
        case Some(xs) => xs
      }
      val newOImporters: Option[List[Importer]] =
        Some(SlickMysql::existingImporters)
      r.copy(oImporters = newOImporters)
    }(ec)
  }
}
