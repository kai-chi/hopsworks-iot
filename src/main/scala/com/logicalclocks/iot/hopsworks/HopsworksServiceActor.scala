package com.logicalclocks.iot.hopsworks

import akka.actor.Actor
import akka.actor.Props
import com.logicalclocks.iot.hopsworks.HopsworksServiceActor.StartHopsworksServer
import com.logicalclocks.iot.hopsworks.webserver.HopsworksServer

class HopsworksServiceActor(host: String, port: Int) extends Actor {

  val hopsworksServer = HopsworksServer(host, port)

  def receive: Receive = {
    case StartHopsworksServer =>
      hopsworksServer.start
  }
}

object HopsworksServiceActor {
  def props(host: String, port: Int): Props = Props(new HopsworksServiceActor(host, port))

  final object StartHopsworksServer
}
