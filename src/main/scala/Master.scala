

/**
 * @author fiona
 */
import akka.actor.{ActorRef, Actor,Props}
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class Master (nrOfWorkers: Int, topology: String, algorithm :String) extends Actor{
      val actors = new Array[ActorRef](nrOfWorkers) 
      var numOfActive=0
      var numFinished=0
      val rand= new Random()
      //if(algorithm.equals("gossip")){
    	  for(i <- 0 until nrOfWorkers){
    		  actors(i) = context.actorOf(Props(new Worker(self, i)))
    	  }
      //}
      
      if(topology.equals("full")){
        for(i <- 0 until nrOfWorkers){
          var neighbors =ArrayBuffer[ActorRef]()
          
          for(j <- 0 until nrOfWorkers){
            if(j!=i){
              neighbors.append(actors(j))
            }
          }
          actors(i) ! Neighbors(neighbors)
        }
      }
      else if(topology.equals("line")){
        for(i <- 0 until nrOfWorkers){
          var neighbors =ArrayBuffer[ActorRef]()
          if(i==0)
              neighbors.append(actors(i+1))
          else if(i==(nrOfWorkers-1))
              neighbors.append(actors(i-1))
          else{
             neighbors.append(actors(i-1))
             neighbors.append(actors(i+1))
          }
          actors(i) ! Neighbors(neighbors)
        }
      }
      else if(topology.equals("3D")){
        generation3DNeighbor
      }
      else if(topology.equals("imp3D")){
        generationImp3DNeighbor
      }
      //-------------------------------------------------------------
      val b=System.currentTimeMillis()
      
      if (algorithm.equals("gossip")){
        actors(rand.nextInt(nrOfWorkers)) ! Roumor  
      } 
      if(algorithm.equals("push-sum")){
        actors(rand.nextInt(nrOfWorkers)) ! PushSum(0,0) 
      }
      //-------------------------------------------------------------      
      def receive = {
        case Participate =>
          
          numOfActive += 1
          //println(numOfActive+ "workers know")
          if(numOfActive==nrOfWorkers)
          {
            println(System.currentTimeMillis()-b)
            context.stop(self)
            context.system.shutdown()
          }
        
        case SumFinish =>
          numFinished += 1
          if (numFinished % 10 == 0)
            println(numFinished)
          if(numFinished == nrOfWorkers - 1){
            println(System.currentTimeMillis()-b)
            context.stop(self)
            context.system.shutdown() 
          }
          // if (numFinished % 10 == 0)
          //   println(numFinished)
        
        case str: String =>
          println(str)
        // case done =>
        //   nrFinished = nrFinished + 1
        //   if(nrFinished == nrOfWorkers){
        //     println("Finished")
        //   }
      }
      
      def generation3DNeighbor = {
          val base = Math.round(Math.pow(nrOfWorkers, 1/3.0)).toInt
          val coordinates = new Array[Points](nrOfWorkers)
          var count=0
          for( x <- 0 until base){
            for( y <- 0 until base){
              for( z <- 0 until base){
                coordinates(count)=new Points(x,y,z)
                count +=1
              }
            }             
          }              

          for(i <- 0 until nrOfWorkers){
             var neighbors=ArrayBuffer[ActorRef]();
             val coor=coordinates(i)
             for(j <- 0 until nrOfWorkers){
               if(j!=i){
                 val temp=coordinates(j)
                 
                 if((coor.x==temp.x && coor.y==temp.y && Math.abs(coor.z-temp.z).toInt==1) || (coor.x==temp.x && coor.z==temp.z && Math.abs(coor.y-temp.y).toInt==1) || (coor.z==temp.z && coor.y==temp.y && Math.abs(coor.x-temp.x).toInt==1))
                   //println("here")
                   neighbors.append(actors(j))
               }
               //println(neighbors.length)
               actors(i) ! Neighbors(neighbors)
             }
          }        
      }
      
      def generationImp3DNeighbor = {
          val base = Math.round(Math.pow(nrOfWorkers, 1/3.0)).toInt
          val coordinates = new Array[Points](nrOfWorkers)
          val rand= new Random()
          var count=0
          for( x <- 0 until base){
            for( y <- 0 until base){
              for( z <- 0 until base){
                coordinates(count)=new Points(x,y,z)
                count +=1
              }
            }             
          } 
          for(i <- 0 until nrOfWorkers){
             var neighbors=ArrayBuffer[ActorRef]();
             var randNeigh=ArrayBuffer[Int]()
             val coor=coordinates(i)
             for(j <- 0 until nrOfWorkers){
               if(j!=i){
                 val temp=coordinates(j)
                 if((coor.x==temp.x && coor.y==temp.y && Math.abs(coor.z-temp.z).toInt==1) || (coor.x==temp.x && coor.z==temp.z && Math.abs(coor.y-temp.y).toInt==1) || (coor.z==temp.z && coor.y==temp.y && Math.abs(coor.x-temp.x).toInt==1))
                   neighbors.append(actors(j))
                 else randNeigh.append(j)
               }               
             }
             val randomIndex= rand.nextInt(randNeigh.length)
             neighbors.append(actors(randNeigh(randomIndex)))
             actors(i) ! Neighbors(neighbors)
          }        
      }
}