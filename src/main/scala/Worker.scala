

/**
 * @author fiona
 */
import akka.actor._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.concurrent.duration._
import scala.math


class Worker(climaster :ActorRef, id:Int) extends Actor{
  var neighbors = ArrayBuffer[ActorRef]()
  var times = 0
  var sp:Double = id + 1
  var wp = 1.0
  val rand= new Random 
  //var prev = 0.0
  var st = true
  var prev = 0.0;
  val MIN = 0.0000000001
  val task= new Runnable{
    def run(){
       //println(neighbors.length)
       val index=rand.nextInt(neighbors.length)
       neighbors(index) ! Roumor
    }
  }
  var timer: Cancellable =null
  implicit val executor =context.system.dispatcher
  
  def startTransmit(){
    climaster ! Participate
    timer=context.system.scheduler.schedule(initialDelay = Duration(0, MILLISECONDS),interval = Duration(10, MILLISECONDS),runnable=task )(executor)
  }

  val pushSumTask= new Runnable{
    def run(){
      sp = sp/2
      wp = wp/2
      prev = sp/wp
      neighbors(rand.nextInt(neighbors.length)) ! PushSum(sp, wp)
    }
  }

  def startPushSum(){
    timer=context.system.scheduler.schedule(initialDelay = Duration(0, MILLISECONDS),interval = Duration(10, MILLISECONDS),runnable=pushSumTask )(executor)
  }
	

  def receive = {
    case Neighbors(neighs) =>
      neighbors.appendAll(neighs)
      
    case Roumor =>
      times +=1
      if(times==1)
        startTransmit()

      else if(times>=10){
        climaster ! Participate
        timer.cancel();
        //println("stop an actor")
        context.stop(self)
      }

    case PushSum(sr, wr) =>    
      sp += sr
      wp += wr
      val current = sp / wp
  //    println(this+ "pre ration:" + ratio + "   --- cur ration " + tmp)
      if( Math.abs(prev - current) < MIN){
        times += 1
        if( times >= 3){
          if(times == 3){
            //println(id + ": ratio is " + current)
            climaster ! SumFinish
          }    
          // timer.cancel()
          // context.stop(self)          
        }
      }else{
        times = 0
      }

      if(st){
        startPushSum()
        st = false 
      }

	  case str: String =>
	    println(str)
	}  
}