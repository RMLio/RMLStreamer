package io.rml.framework.util.logging

import io.rml.framework.core.internal.Logging

object Logger extends ColorString with Logging {

  override def logInfo(msg : String): Unit = {
    super.logInfo( "[INFO]  ".yellow +  msg.yellow)
  }

  override def logError(msg: String): Unit = {
    super.logError("[ERROR]  ".red + msg.red )
  }

  def logSuccess(msg: String): Unit = {
    super.logInfo("[SUCCESS]  ".green + msg.green)
  }

  def lineBreak(count:Int = 100) : Unit = {
    super.logInfo(("="*count).blue)
  }


}
