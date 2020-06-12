package io.rml.framework.model.function

import java.io.File

import io.rml.framework.StaticTestSpec
import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.model.{DynamicMethodFunction, Function}
import io.rml.framework.core.function.std.StdFunctionLoader
import io.rml.framework.core.function.{FunctionLoader, FunctionUtils}
import io.rml.framework.core.model.Uri
import io.rml.framework.core.vocabulary.{Namespaces, RMLVoc}

class FunctionLoaderTest extends StaticTestSpec {
  val functionMappingFile = new File(getClass.getClassLoader.getResource("grel_java_mapping.ttl").getFile)


  "FunctionLoader" should "initialize the transformation lazily and correctly" in {
    val loader = FunctionLoader().parseFunctions(functionMappingFile)
    assert(loader.isInstanceOf[StdFunctionLoader])

//    val test = Uri("http://users.ugent.be/~bjdmeest/function/grel.ttl#toUpperCase")
//    val transformation = loader.loadFunction(test)
//
//    assume(transformation.isInstanceOf[Option[DynamicMethodFunction]])
  }

}
