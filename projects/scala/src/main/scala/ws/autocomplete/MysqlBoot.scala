package ws.autocomplete

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import utils.Settings
import ws.autocomplete.spraythings.ServiceActor

import scala.concurrent.duration._


object MysqlBoot extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[ServiceActor], "autocomplete-service")
  implicit val timeout = Timeout(500 milliseconds)

  // start a new HTTP server with our service actor as the handler
  val settings = new Settings()
  IO(Http) ? Http.Bind(
    service,
    interface = settings.Autocomplete.Spray.ip,
    port = settings.Autocomplete.Spray.port)
}
