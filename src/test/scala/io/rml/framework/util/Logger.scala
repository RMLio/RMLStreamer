package io.rml.framework.util


object Logger extends ColorString {

  

  def logInfo(msg : String): Unit = {
    println( "[INFO]".yellow +  msg.yellow)
  }

  def logError(msg: String): Unit = {
    println("[ERROR]".red + msg.red )
  }

  def logSuccess(msg: String): Unit = {
    println("[SUCCESS]".green + msg.green)
  }

  def lineBreak(count:Int = 100) : Unit = {
    println(("="*count).blue)
  }


}
