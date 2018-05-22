package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import soot.RefType
import soot.Type
import soot.Value

/**
 * Creates a simple mock of a [Value].
 */
fun mockValue() =
    mock<Value> {
        on { it.type } doReturn RefType.v("java.lang.Object")
    }

/**
 * Creates a mock of a [Value] such that no such two mocks equal each other.
 */
fun mockTypedValue() =
    mock<Value> {
        on { it.type } doReturn mock<Type> {}
    }
