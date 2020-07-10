package io.rml.framework.core.function.flink

import java.net.URLClassLoader

import io.rml.framework.Main.logError
import io.rml.framework.api.FnOEnvironment
import io.rml.framework.flink.item.Item
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration

import scala.util.{Failure, Success, Try}

/**
 * AbstractRichIdentityFunction-subtypes do not alter or access the input data.
 * They can be used to access the runtime context before or after the map-function is called,
 * using the .open() and .close() inherited from RichMapFunction, respectively.
 *
 *
 * @tparam T
 */
abstract class AbstractRichIdentityFunction[T] extends RichMapFunction[T,T] {
  /**
   * Identity. Passes input to output.
   * @param value
   * @return
   */
  override def map(value: T): T = value
}

/**
 * Trait for RichMapFunction that use streams (i.e. Iterables of types IN|OUT)
 * @tparam IN: input type of
 * @tparam OUT
 */
trait RichStreamFunction[IN,OUT] extends RichMapFunction[Iterable[IN], Iterable[OUT]]

/**
 *
 * @tparam T
 */
abstract class AbstractRichIdentityStreamFunction[T] extends RichStreamFunction[T,T] {
  override def map(value: Iterable[T]): Iterable[T] = value
}

/**
 * Concrete class that only passes a stream of [[Item]]
 */
class RichStreamItemIdentityFunction extends AbstractRichIdentityStreamFunction[Item]

/**
 * Concrete class that only passes [[Item]]
 */
class RichItemIdentityFunction extends AbstractRichIdentityFunction[Item]

/**
 *
 * Looks for the given list of [[jarNames]] using the runtime context's classloader which is used
 * to load the given [[classNames]] into the [[FnOEnvironment]].
 *
 * @param jarNames
 * @param classNames
 */
class AbstractFnOEnvironmentLoader[T](jarNames : List[String],
                                 classNames : List[String]) extends AbstractRichIdentityFunction[T] {
  override def open(parameters: Configuration): Unit = {

    /**
     * It usually works to put the job’s JAR file into the /lib directory. The JAR
     * will be part of both the classpath (the AppClassLoader) and the dynamic class
     * loader (FlinkUserCodeClassLoader). Because the AppClassLoader is the parent of
     * the FlinkUserCodeClassLoader (and Java loads parent-first, by default), this
     * should result in classes being loaded only once.
     * Ref: https://ci.apache.org/projects/flink/flink-docs-stable/monitoring/debugging_classloading.html#avoiding-dynamic-classloading-for-user-code
     */

    // 1. find resource URLs using UserCodeClassLoader
    val ucl = getRuntimeContext.getUserCodeClassLoader
    val resourceURLs = jarNames.map(jn=>ucl.getResource(jn))

    // 2. initialize URLClassLoader with resource URLs from step (1.)
    val classLoader = new URLClassLoader(resourceURLs.toArray, ucl)

    // 3. load the classes into the FnOEnvironment
    classNames.foreach{
      cn => {
        // add class object to FnOEnvironment if not existent
        if(!FnOEnvironment.loadedClassesMap.contains(cn)) {
          //val cls = classLoader.loadClass(cn)
          Try(classLoader.loadClass(cn)) match {
            case Success(cls) =>FnOEnvironment.loadedClassesMap.put(cn, cls)
            case Failure(exception) => logError(s"Unable to load ${cn} into FnO Environment. Details: ${exception.getMessage}")
          }
        }
      }
    }
    super.open(parameters)
  }
}

/**
 * [[FnOEnvironmentStreamLoader]] can be used in a DataStream[Item].
 * Looks for the given list of [[jarNames]] using the runtime context's classloader which is used
 * to load the given [[classNames]] into the [[FnOEnvironment]].
 *
 * @param jarNames
 * @param classNames
 */
class FnOEnvironmentStreamLoader(jarNames : List[String], classNames : List[String])
  extends AbstractFnOEnvironmentLoader[Iterable[Item]](jarNames = jarNames,classNames = classNames)
/**
 * [[FnOEnvironmentLoader]] can be used in a DataSet[Item].
 * Looks for the given list of [[jarNames]] using the runtime context's classloader which is used
 * to load the given [[classNames]] into the [[FnOEnvironment]].
 *
 * @param jarNames
 * @param classNames
 */
class FnOEnvironmentLoader(jarNames : List[String], classNames : List[String])
  extends AbstractFnOEnvironmentLoader[Item](jarNames = jarNames,classNames = classNames)
