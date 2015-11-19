/**
 * @author fiona
 */

 
import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Main{

  
  def main(args: Array[String]){
    var numNodes=40
    var topology="full"
    var algorithm="push-sum"
    
    if(args.length < 1){
      println("The default number of actors involved is " + numNodes)
      println("The default topology is "+topology)
      println("The default algorithm is "+algorithm)
    }
    else{
      numNodes=args(0).toInt
      topology=args(1)
      algorithm=args(2)
      println("The number of actors involved is "+ numNodes)
      println("The topology is "+topology)
      println("The algorithm is "+algorithm)
    }
    
    if(topology.contains("3D")){
      numNodes=Math.round(Math.pow(numNodes, 1/3.0)).toInt
      if(numNodes<=1)
        numNodes=2
      numNodes=Math.pow(numNodes, 3).toInt
      println(numNodes)
    }
    
    val system = ActorSystem("GossipSystem",ConfigFactory.load(ConfigFactory.parseString("""
        akka {
           log-dead-letters = off
        }
        """)))

        // create the master
        val master = system.actorOf(Props(new Master(numNodes, topology, algorithm)), name = "master")
  }
}