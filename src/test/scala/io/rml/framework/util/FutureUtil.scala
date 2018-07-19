package io.rml.framework.util

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object FutureUtil {


  def lift[T](futures: Seq[Future[T]]) (implicit exec: ExecutionContext): Seq[Future[Try[T]]] = {
    futures.map(_.map {
      Success(_)
    }.recover { case t => Failure(t) })
  }


  def waitAll[T] (futures: Seq[Future[T]])(implicit exec: ExecutionContext): Future[Seq[Try[T]]] = Future.sequence(lift(futures))


}
