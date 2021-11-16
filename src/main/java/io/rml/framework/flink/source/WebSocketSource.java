package io.rml.framework.flink.source;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

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
 *
 * @author Gerald Haesendonck
 **/
public class WebSocketSource extends RichSourceFunction<String> {
	private static Logger log = LoggerFactory.getLogger(WebSocketSource.class);
	private final String uri;
	private boolean stop = false;

	public WebSocketSource(String uri) {
		this.uri = uri;
	}

	public void run(SourceContext<String> sourceContext) throws Exception {
		HttpClient
				.newHttpClient()
				.newWebSocketBuilder()
				.buildAsync(URI.create(uri), new WebSocketClient(sourceContext))
				.join();

		while (!stop) {
			Thread.sleep(500);
		}
	}

	public void cancel() {
		stop = true;
	}

	private static class WebSocketClient implements WebSocket.Listener {
		private final SourceContext<String> context;

		private StringBuilder message = new StringBuilder();

		private WebSocketClient(SourceContext<String> context) {
			this.context = context;
		}

		@Override
		public void onOpen(WebSocket webSocket) {
			log.debug("onOpen using subprotocol " + webSocket.getSubprotocol());
			WebSocket.Listener.super.onOpen(webSocket);
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			log.debug("onText received {}", data);
			message.append(data);
			if (last) {
				log.debug("message: {}", message);
				context.collect(message.toString());
				message = new StringBuilder();
			}
			return null;
		}

		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			log.warn("WebSocket error: {}", webSocket.toString(), error);
		}
	}
}
