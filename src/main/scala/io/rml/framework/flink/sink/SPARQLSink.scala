package io.rml.framework.flink.sink

import io.rml.framework.Main.logError
import org.apache.commons.io.IOUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.concurrent.FutureCallback
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy
import org.apache.http.impl.nio.client.{CloseableHttpAsyncClient, HttpAsyncClients}

import java.io.IOException
import java.nio.charset.StandardCharsets


class SPARQLSink private[sink](val URL: String) extends RichSinkFunction[String] {
  private var httpClient: CloseableHttpAsyncClient = _

  @throws[Exception]
  override def open(parameters: Configuration): Unit = {
    httpClient = HttpAsyncClients.custom
      .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
      .disableCookieManagement
      .build
    httpClient.start()
  }

  @throws[Exception]
  override def invoke(value: String, context: SinkFunction.Context): Unit = {
    val httpPost = new HttpPost(URL)
    httpPost.setEntity(new StringEntity(s"INSERT DATA {$value}"))
    httpPost.setHeader("Content-Type", "application/sparql-update")
    httpClient.execute(httpPost, new FutureCallback[HttpResponse] {
      override def completed(result: HttpResponse): Unit = {
        val httpStatusCode = result.getStatusLine.getStatusCode
        if (httpStatusCode >= 300) {
          val body = result.getEntity
          if (body.getContentLength > 0) {
            val inputStream = body.getContent
            try {
              val contents = new String(IOUtils.toByteArray(inputStream), StandardCharsets.UTF_8)
              logError("Error while sending SPARQL request: " + contents)
            } catch {
              case e: IOException => logError("Error while sending SPARQL request")
            } finally if (inputStream != null) inputStream.close()
          }
        }
      }

      override def failed(ex: Exception): Unit = {
        logError("Error while sending SPARQL request")
      }

      override def cancelled(): Unit = {
        logError("Error while sending SPARQL request")
      }
    })
  }

  @throws[Exception]
  override def close(): Unit = {
    httpClient.close()
  }
}
