package com.logicalclocks.leshan.listeners

import akka.actor.ActorRef
import com.logicalclocks.leshan.LeshanActor.NewObserveResponse
import org.eclipse.leshan.core.observation.Observation
import org.eclipse.leshan.core.response.ObserveResponse
import org.eclipse.leshan.server.observation.ObservationListener
import org.eclipse.leshan.server.registration.Registration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

case class HopsObservationListener(leshanActor: ActorRef) extends ObservationListener {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  def newObservation(observation: Observation, registration: Registration): Unit = {
    logger.debug(s"New observation $observation for $registration")
  }

  def cancelled(observation: Observation): Unit = {

  }

  def onResponse(observation: Observation, registration: Registration, response: ObserveResponse): Unit = {
    leshanActor ! NewObserveResponse(registration.getId, response)
  }

  def onError(observation: Observation, registration: Registration, error: Exception): Unit = {

  }
}