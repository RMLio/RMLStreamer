package io.rml.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * MIT License
 * <p>
 * Copyright (C) 2017 - 2021 RDF Mapping Language (RML)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **/
public class TestTCPServer implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(TestTCPServer.class);
	private final int port;
	private final BlockingQueue<String> mQueue = new ArrayBlockingQueue<>(64);
	private boolean stop = false;

	public TestTCPServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			log.info("TCP server listens on port {}", port);
			serverSocket.setSoTimeout(20000);
			while(!stop) {
				Socket socket = serverSocket.accept();
				log.info("New client connected");
				SocketHandler socketHandler = new SocketHandler(socket, mQueue);
				Thread socketHandlerThread = new Thread(socketHandler, "SocketHandler");
				socketHandlerThread.start();
			}
		} catch (IOException e) {
			log.error("An error occurred. ", e);
		}
	}

	public void send(final String message) throws InterruptedException {
		log.debug("Putting message: {} on queue", message);
		mQueue.put(message);
	}

	public void stop() {
		log.info("Stopping TCPServer...");
		stop = true;
	}

	///// socket accept handling
	private static class SocketHandler implements Runnable {
		private final Socket socket;
		private final BlockingQueue<String> mQueue;

		private SocketHandler(Socket socket, BlockingQueue<String> mQueue) {
			this.socket = socket;
			this.mQueue = mQueue;
		}

		@Override
		public void run() {
			try {
				OutputStream out = socket.getOutputStream();
				String msg = "";
				while (msg != null) {
					msg = mQueue.poll(3, TimeUnit.SECONDS);
					if (msg != null) {
						log.debug("Sending message {}", msg);
						out.write(msg.getBytes(StandardCharsets.UTF_8));
					} else {
						log.debug("Message time-out.");
					}
				}
			} catch (IOException e) {
				log.error("An error ocurred writing to socket: ", e);
			} catch (InterruptedException e) {
				log.warn("TCP Server socket handler interrupted. ", e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					log.warn("Could not close socket. ", e);
				}
			}
		}
	}

}
