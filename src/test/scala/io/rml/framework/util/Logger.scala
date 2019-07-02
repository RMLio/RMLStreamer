package io.rml.framework.util

import io.rml.framework.core.internal.Logging

object Logger extends ColorString with Logging {

  override def logInfo(msg : String): Unit = {
    super.logError( "[INFO]  ".yellow +  msg.yellow)
  }

  override def logError(msg: String): Unit = {
    super.logError("[ERROR]  ".red + msg.red )
  }

  def logSuccess(msg: String): Unit = {
    super.logError("[SUCCESS]  ".green + msg.green)
  }

  def lineBreak(count:Int = 100) : Unit = {
    super.logError(("="*count).blue)
  }


}
