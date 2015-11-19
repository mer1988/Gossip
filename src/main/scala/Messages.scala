

/**
 * @author fiona
 */
import akka.actor.{ActorRef, Actor}
import scala.collection.mutable.ArrayBuffer

case class Neighbors(neighbors: ArrayBuffer[ActorRef])
case object Participate
case class PushSum(s: Double, w: Double)
case object Roumor
case object SumStart
case object SumFinish
