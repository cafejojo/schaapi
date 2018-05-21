package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.filters

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import soot.SootClass
import soot.SootMethod
import soot.jimple.InvokeExpr

internal const val NON_LIBRARY_CLASS = "java.lang.String"
internal const val LIBRARY_CLASS =
    "org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple.testclasses.library.Object1"

internal fun constructInvokeExprMock(declaringClassName: String): InvokeExpr {
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
