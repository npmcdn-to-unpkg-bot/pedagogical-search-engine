package ws.indices

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import utils.Settings
import ws.indices.spraythings.ServiceActor

import scala.concurrent.duration._


object MysqlBoot extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[ServiceActor], "indices-service")
  implicit val timeout = Timeout(2000 milliseconds)

  // start a new HTTP server with our service actor as the handler
  val settings = new Settings()
  IO(Http) ? Http.Bind(
    service,
    interface = settings.Indices.Ws.Spray.ip,
    port = settings.Indices.Ws.Spray.port)
}
