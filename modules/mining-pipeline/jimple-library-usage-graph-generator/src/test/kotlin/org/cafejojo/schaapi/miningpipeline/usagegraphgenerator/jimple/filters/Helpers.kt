package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import soot.SootClass
import soot.SootMethod
import soot.jimple.StaticInvokeExpr

internal const val NON_LIBRARY_CLASS = "java.lang.String"
internal const val LIBRARY_CLASS =
    "org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1"

internal fun constructInvokeExprMock(declaringClassName: String): StaticInvokeExpr {
    val clazz = mock<SootClass> {
        on { name } doReturn declaringClassName
    }
    val method = mock<SootMethod> {
        on { declaringClass } doReturn clazz
    }
    return mock {
        on { getMethod() } doReturn method
    }
}
