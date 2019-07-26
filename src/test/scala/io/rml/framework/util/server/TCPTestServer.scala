package io.rml.framework.util.server

import java.net.InetSocketAddress

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.CharsetUtil
import io.rml.framework.util.logging.Logger

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

case class TCPTestServer(port: Int = 9999) extends TestServer {

  val lock: AnyRef with Specializable = AnyRef
  var promiseChContext: Promise[ChannelHandlerContext] = Promise[ChannelHandlerContext]()
  var serverChannel: Option[ChannelFuture] = None

  override def setup(): Unit = {
    setup(port)
  }

  def setup(port: Int, messages: Iterator[String] = Iterator.empty): Unit = {
    val group = new NioEventLoopGroup

    try {
      val serverBootstrap = new ServerBootstrap()
      serverBootstrap.group(group)

      serverBootstrap.channel(classOf[NioServerSocketChannel])
      serverBootstrap.localAddress(new InetSocketAddress("localhost", port))
      serverBootstrap.childHandler(new ChannelInitializer[SocketChannel]() {
        @throws[Exception]
        override protected def initChannel(socketChannel: SocketChannel): Unit = {
          socketChannel.pipeline.addLast(
            new TCPServerHandler(messages))
        }
      })


      serverChannel = Some(serverBootstrap.bind.sync)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  override def writeData(messages:  List[TestData])(implicit executur: ExecutionContextExecutor): Unit = {
    //TODO: Start new connection for every batch if required!!
    getChCtxFuture map { ctx =>
      Logger.logInfo(ctx.channel().toString)
      for (batch <- messages) {
        for (el <- batch.data) {
          el.split("\n").foreach(Logger.logInfo)
          val byteBuff = ctx.alloc.buffer(el.length)
          byteBuff.writeBytes(el.getBytes())
          ctx.channel.writeAndFlush(byteBuff)
          Thread.sleep(2000)
        }
      }
    }
  }

  override def tearDown(): Unit = {
    if (serverChannel.isDefined) {
      val ch = serverChannel.get

      ch.channel().close().sync()
    }
  }


  def getChCtxFuture: Future[ChannelHandlerContext] = lock.synchronized {
    promiseChContext.future
  }

  class TCPServerHandler(messages: Iterator[String]) extends ChannelInboundHandlerAdapter {

    import io.netty.buffer.ByteBuf

    @throws[Exception]
    override def channelRead(ctx: ChannelHandlerContext, msge: Any): Unit = {
      val inBuffer = msge.asInstanceOf[ByteBuf]
      val received = inBuffer.toString(CharsetUtil.UTF_8)

      System.out.println("Server received: " + received)

    }


    @throws[Exception]
    override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
      ctx.flush()
    }

    override def channelActive(ctx: ChannelHandlerContext): Unit = {
      lock.synchronized {


        if (promiseChContext.isCompleted) {
          promiseChContext = Promise[ChannelHandlerContext]()
        }
        promiseChContext success ctx

        messages.foreach(msg => {
          val byteBufMsg = ctx.alloc.buffer(msg.length)
          byteBufMsg.writeBytes(msg.getBytes)
          ctx.channel.writeAndFlush(byteBufMsg)
        })
      }
    }

    @throws[Exception]
    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
      cause.printStackTrace()
      ctx.close
    }
  }


}
