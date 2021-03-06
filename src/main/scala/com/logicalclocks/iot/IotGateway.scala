package com.logicalclocks.iot

import akka.actor.ActorRef
import akka.actor.ActorSystem
import com.logicalclocks.iot.db.InMemoryBufferServiceActor
import com.logicalclocks.iot.hopsworks.HopsworksServiceActor
import com.logicalclocks.iot.hopsworks.HopsworksServiceActor.StartHopsworksServer
import com.logicalclocks.iot.kafka.ProducerServiceActor
import com.logicalclocks.iot.leshan.LeshanActor
import com.logicalclocks.iot.leshan.LeshanActor.StartServer
import com.logicalclocks.iot.leshan.LeshanConfig
import org.eclipse.leshan.LwM2m
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scopt.OParser

object IotGateway extends App {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  val builder = OParser.builder[Config]
  val parser1 = {
    import builder._
    OParser.sequence(
      programName("Hops IoT Gateway"),
      head("Hops IoT Gateway"),
      opt[String]('l', "coapshost")
        .action((x, c) => c.copy(coapsHost = x))
        .text("Set the secure local CoAP address. Default: localhost"),
      opt[Int]('p', "coapsport")
        .action((x, c) => c.copy(coapsPort = x))
        .text("Set the secure local CoAP port. Default: 5684"),
      help('h', "help")
        .text("Print the help message"))
  }

  val system: ActorSystem = ActorSystem("iotGateway")

  val leshanConfig: LeshanConfig = OParser.parse(parser1, args, Config()) match {
    case Some(config) =>
      logger.info(config.toString)
      LeshanConfig(
        config.coapsHost,
        config.coapsPort,
        "",
        5683)
    case _ =>
      throw new Error("Argument parse error")
  }

  val dbActor: ActorRef =
    system.actorOf(InMemoryBufferServiceActor.props())

  val producerActor: ActorRef =
    system.actorOf(ProducerServiceActor.props(dbActor))

  val leshanActor: ActorRef =
    system.actorOf(LeshanActor.props(leshanConfig, dbActor))

  val hopsworksActor: ActorRef =
    system.actorOf(HopsworksServiceActor.props("localhost", 12222))

  leshanActor ! StartServer
  hopsworksActor ! StartHopsworksServer
}

case class Config(
  coapsHost: String = "",
  coapsPort: Int = LwM2m.DEFAULT_COAP_SECURE_PORT)
