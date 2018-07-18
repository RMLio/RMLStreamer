package io.rml.framework

import java.io.File
import java.net.InetSocketAddress

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.util.CharsetUtil
import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.model.FormattedRMLMapping
import io.rml.framework.helper.Logger

object TestUtil {
  var biggerContext: ChannelHandlerContext = _
  def getContext : ChannelHandlerContext = biggerContext
  def readMapping(path: String): FormattedRMLMapping = {
    val classLoader = getClass.getClassLoader
    val file_1 = new File(path)
    val mapping = if (file_1.isAbsolute) {
      val file = new File(path)
      MappingReader().read(file)
    } else {
      val file = new File(classLoader.getResource(path).getFile)
      MappingReader().read(file)
    }

    FormattedRMLMapping.fromRMLMapping(mapping)
  }

  def createTCPServer(port: Int, messages: Iterator[String] = Iterator.empty): Unit = {
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


      val channelFuture = serverBootstrap.bind.sync

      channelFuture.channel().closeFuture().sync()
    } catch {
      case e: Exception =>
        e.printStackTrace(); null
    }
  }

  class TCPServerHandler(messages: Iterator[String]) extends ChannelInboundHandlerAdapter {

    private var bigContext:ChannelHandlerContext =  _
    import io.netty.buffer.{ByteBuf, Unpooled}

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
      bigContext = ctx
      messages.foreach(msg => {
        val byteBufMsg = ctx.alloc.buffer(msg.length)
        byteBufMsg.writeBytes(msg.getBytes)
        ctx.channel.writeAndFlush(byteBufMsg)
      })
    }

    @throws[Exception]
    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
      cause.printStackTrace()
      ctx.close
    }
  }

}
