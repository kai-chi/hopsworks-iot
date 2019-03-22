package com.logicalclocks.lwm2m

import org.eclipse.leshan.core.node.LwM2mObject
import org.eclipse.leshan.core.node.LwM2mObjectInstance
import org.eclipse.leshan.core.response.ObserveResponse

import scala.annotation.tailrec
import scala.collection.JavaConverters._


case class ObserveResponseUnwrapper(
  timestamp: Long,
  endpointClientName: String,
  response: ObserveResponse) {

  def getIpsoObjectList(): List[IpsoObjectMeasurement] =
    response.getContent.getId match {
      case 3303 =>
        val instances: Iterable[LwM2mObjectInstance] =
          response
            .getContent
            .asInstanceOf[LwM2mObject]
            .getInstances
            .asScala
            .values
        extractTempFromInstances(endpointClientName, instances, timestamp)
      case _ => throw new Error("Unknown ipso object")
    }

  private def extractTempFromInstances(endpointClientName: String, instances: Iterable[LwM2mObjectInstance], timestamp: Long): List[IpsoObjectMeasurement] = {
    @tailrec
    def iter(instances: Iterable[LwM2mObjectInstance], measurements: List[IpsoObjectMeasurement]): List[IpsoObjectMeasurement] = {
      instances.headOption match {
        case None => measurements
        case Some(i) =>
          val resources = i
            .getResources
            .asScala
            .mapValues(_.getValue)
          val obj = TempIpsoObjectMeasurement(
            timestamp,
            endpointClientName,
            resources(5700).asInstanceOf[Double],
            resources.get(5601).map(_.asInstanceOf[Double]),
            resources.get(5602).map(_.asInstanceOf[Double]),
            resources.get(5603).map(_.asInstanceOf[Double]),
            resources.get(5604).map(_.asInstanceOf[Double]),
            resources.get(5701).map(_.asInstanceOf[String]),
            resources.get(5605).map(_.asInstanceOf[Boolean]))
          iter(instances.tail, measurements :+ obj)
      }
    }

    iter(instances, List.empty)
  }

}