package io.rml.framework.core.model.std

import io.rml.framework.core.model.{Entity, JoinConfigMap, Literal, Uri}
import io.rml.framework.engine.composers.{JoinType, TumblingJoin}
import io.rml.framework.engine.windows.{TumblingWindow, WindowType}

case class StdJoinConfigMap(identifier:String, joinType:JoinType = TumblingJoin, windowType: Option[WindowType]=Some(TumblingWindow))  extends  JoinConfigMap{
  /**
   *
   * @return
   */
  override def constant: Option[Entity] = None

  /**
   *
   * @return
   */
  override def reference: Option[Literal] = None

  /**
   *
   * @return
   */
  override def template: Option[Literal] = None

  /**
   *
   * @return
   */
  override def termType: Option[Uri] = None


}
