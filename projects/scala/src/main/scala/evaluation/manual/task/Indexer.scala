package evaluation.manual.task

import evaluation.manual.json.Annotation

import scala.concurrent.Future

trait Indexer {
  def index(annotation: Annotation): Future[Annotation]
}
