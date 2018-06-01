package org.cafejojo.schaapi.miningpipeline.patterndetector.prefixspan

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import soot.RefType
import soot.Value
import java.util.UUID

/**
 * A [Value] that does not contain other values, and that implements only functionality related to equivalence.
 */
fun mockValue(type: String): Value = mock {
    on { it.type } doReturn RefType.v(type)
    on { it.equivTo(any()) } doAnswer { answer ->
        val other = answer.arguments[0]

        other is Value && it.type == other.type
    }
    on { it.equivHashCode() } doReturn type.hashCode()
}

/**
 * A simple implementation of [Value] that is not equal or equivalent to any other [Value].
 */
fun mockUniqueValue() = mockValue(UUID.randomUUID().toString())
