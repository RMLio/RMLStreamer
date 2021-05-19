/**
  * MIT License
  *
  * Copyright (C) 2017 - 2020 RDF Mapping Language (RML)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  **/
package io.rml.framework

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelFuture, ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer}
import io.netty.util.{CharsetUtil, ReferenceCountUtil}
import io.rml.framework.util.logging.Logger
import io.rml.framework.util.server.TestData

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

class TCPStreamTestSyncFnO extends StreamTestSync {
  var serverChannel: ChannelFuture = _
  var group: NioEventLoopGroup = _
  var lock: AnyRef with Specializable = _
  var promiseChContext: Promise[ChannelHandlerContext] = _

  override def testFolder: String = "stream/fno-testcases/tcp"

  override def passingTests: Array[(String, String)] = Array(
    ("stream/fno-testcases/tcp", "noopt")
  )


  override def setup(): Unit = {
    super.setup()
    FunctionMappingSetup.setupFunctionLoader()
    lock = AnyRef
    promiseChContext = Promise[ChannelHandlerContext]()
    logInfo("Setting up event loop group...")
    group = new NioEventLoopGroup()
    logInfo("Setting up event loop group done.")
    logInfo("Setting up server bootstrap")
    val serverBootstrap = new ServerBootstrap()
    serverBootstrap.group(group)
    serverBootstrap.channel(classOf[NioServerSocketChannel])
    serverBootstrap.localAddress(new InetSocketAddress("localhost", 9999))
    serverBootstrap.childHandler(new ChannelInitializer[SocketChannel]() {
      override protected def initChannel(socketChannel: SocketChannel): Unit = {
        socketChannel.pipeline.addLast(
          new TCPServerHandler(Iterator.empty))
      }
    })

    serverChannel = serverBootstrap.bind.sync

  }

  override def afterTestCase(): Unit = {}

  override def teardown(): Unit = {
    Logger.logInfo("Stopping TCP server")
    if (serverChannel != null) {
      serverChannel.channel().closeFuture().await(10, TimeUnit.SECONDS)
    }
    if (group != null) {
      group.shutdownGracefully(2, 10, TimeUnit.SECONDS).await()
    }
    Logger.logInfo("TCP server stopped.")
  }

  override def writeData(input: List[TestData])(implicit executor: ExecutionContextExecutor): Unit = {
    getChCtxFuture map { ctx =>
      Logger.logInfo(ctx.channel().toString)
      for (batch <- input) {
        for (el <- batch.data) {
          el.split("\n").foreach(Logger.logInfo)
          val bytesToSend = el.getBytes(StandardCharsets.UTF_8)
          val byteBuff = ctx.alloc.buffer(bytesToSend.length)
          byteBuff.writeBytes(bytesToSend)
          ctx.channel.writeAndFlush(byteBuff).await()
        }
      }
    }
  }


  def getChCtxFuture: Future[ChannelHandlerContext] = lock.synchronized {
    promiseChContext.future
  }


  class TCPServerHandler(messages: Iterator[String]) extends ChannelInboundHandlerAdapter {
    @throws[Exception]
    override def channelRead(ctx: ChannelHandlerContext, msge: Any): Unit = {
      val inBuffer = msge.asInstanceOf[ByteBuf]
      val received = inBuffer.toString(CharsetUtil.UTF_8)
      ReferenceCountUtil.release(msge)

      Logger.logInfo("TCPServer received: " + received)

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
      Logger.logError("Error while using TCP server: ", cause)
      ctx.close
    }
  }
}
