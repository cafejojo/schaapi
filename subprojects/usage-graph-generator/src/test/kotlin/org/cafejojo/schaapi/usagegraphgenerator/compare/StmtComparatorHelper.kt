package org.cafejojo.schaapi.usagegraphgenerator.compare

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import soot.Type
import soot.Value

/**
 * Creates a simple mock of a [Value].
 */
fun mockValue() =
    mock<Value> {}

/**
 * Creates a mock of a [Value] such that no such two mocks equal each other.
 */
fun mockTypedValue() =
    mock<Value> {
        on { it.type } doReturn mock<Type> {}
    }
