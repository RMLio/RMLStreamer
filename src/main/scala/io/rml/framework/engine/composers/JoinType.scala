package io.rml.framework.engine.composers

sealed trait JoinType

case object TumblingJoin extends JoinType
case object CrossJoin extends JoinType
case object VelocityAwareJoin extends JoinType


