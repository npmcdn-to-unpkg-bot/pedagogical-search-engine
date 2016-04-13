package ws.autocomplete.fetcher

import slick.jdbc.JdbcBackend
import ws.autocomplete.SearchContext
import ws.autocomplete.query.Queries
import ws.autocomplete.results.Result

import scala.concurrent.{ExecutionContext, Future}

class Jdbc(db: JdbcBackend.Database)(implicit val ec: ExecutionContext) extends Fetcher {
  val _ec = ec
  override def getResults(sc: SearchContext): Future[List[Result]] =
    db.run(Queries.getAction(sc.text, sc.maxRes)).map(_.toList)(_ec)
}
