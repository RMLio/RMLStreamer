package io.rml.framework

import java.util.concurrent.Executors

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.AsyncFlatSpec

import scala.concurrent.{ExecutionContext, Future}


class StreamingTest  extends  AsyncFlatSpec{
  implicit val env = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val executor =  ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val passing = "stream"
  def addSoon(addends: Int*) : Future[Int] = Future{addends.sum}

  "TCPsource -pull " should "map the incoming statements correctly with a valid mapping file" in {

     addSoon(1,2)  map ( sum  => assert(sum == 3))
    val f: Future[List[String]] = Future{

      List[String]()
    }
    succeed
  }

}
