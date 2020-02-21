package io.rml.framework.core.util

/**
  * <p>Copyright 2019 IDLab (Ghent University - imec)</p>
  *
  * @author Gerald Haesendonck
  */
object StreamerConfig {
  /**
    * Enabling this allows data items to be generated from data sources in parallel, per task manager. It adds a keyBy
    * operation to the datastream of Strings to distribute the generated over different task slots. The number of
    * used task slots = min(nr of task slots assigned for the job, nr of task slots on a *single* task manager)
    */
  private var executeLocalParallel: Boolean = false

  /**
    * Enable or disable local parallelism
    * @param parallel true: enable, false: disable
    */
  def setExecuteLocalParallel(parallel: Boolean): Unit = {
    executeLocalParallel = parallel
  }

  /**
    * Check if local parallelism is enabled
    * @return true: enabled, false: disabled
    */
  def isExecuteLocalParallel(): Boolean = {
    executeLocalParallel
  }
}
