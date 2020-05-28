package io.rml.framework.model.function

import java.io.File

import io.rml.framework.StaticTestSpec
import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.model.{DynamicMethodTransformation, Transformation}
import io.rml.framework.core.function.{TransformationLoader, TransformationUtils}
import io.rml.framework.core.model.Uri
import io.rml.framework.core.vocabulary.{Namespaces, RMLVoc}

class FunctionLoaderTest extends StaticTestSpec {
  val functionFile = new File(getClass.getClassLoader.getResource("functions.ttl").getFile)

  "Loading default grel functions classes" should "succeed without error" in {

    val filePath = RMLEnvironment.getClass.getClassLoader.getResource("GrelFunctions.jar").getFile
    val cls = TransformationUtils.loadClassFromJar(new File(filePath), "GrelFunctions")
    println(cls)
    assert(cls.getDeclaredMethods.length > 0, "Declared methods must be more than 0 for now")
  }

  "FunctionLoader" should "initialize the transformation lazily and correctly" in {
    val loader = TransformationLoader().parseTransformations(functionFile)
    val test = Uri("http://users.ugent.be/~bjdmeest/function/grel.ttl#toUpperCase")
    val transformation = loader.loadTransformation(test)

    assume(transformation.isInstanceOf[Option[DynamicMethodTransformation]])
  }


  "Dynamic Function" should "be loaded and executable" in {
    val loader = TransformationLoader().parseTransformations(functionFile)

    val testValue = "qmlsdkfje sdfesdfFJ"
    val functionUri = Uri(Namespaces("grel", "toUpperCase"))

    // map: uri -> trans. metadata
    val transformationMap = loader.getTransformationMap

    // transformation meta data for given function uri
    val transformationMetaData = transformationMap.getOrElse(
      functionUri,
      throw new Exception("Unable to find transformation meta data in transformation map"))


    val transformation = Transformation(functionUri.identifier,transformationMetaData)
    // initializedTransformation is a DynamicMethodTransformation
    val initializedTransformation: Transformation = transformation.initialize()

    // bind value parameter to its actual value
    val paramMap = Map(Uri(Namespaces("grel", "valueParameter")) -> testValue)
    // execute transformation and obtain its results
    val result = initializedTransformation.execute(paramMap).get.head

    assume(result.toString == testValue.toUpperCase())
  }
}
