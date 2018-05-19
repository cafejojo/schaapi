package org.cafejojo.schaapi.patterndetector

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.cafejojo.schaapi.usagegraphgenerator.SootNode
import soot.Type
import soot.Value
import soot.jimple.DefinitionStmt

/**
 * Create a soot node that returns a mock of a [Value] such that by default no two values have the same type.
 */
fun mockSootNode(valueTypeLeft: Type? = null, valueTypeRight: Type? = null): SootNode {
    val leftOp = mockTypedValue(valueTypeLeft)
    val rightOp = mockTypedValue(valueTypeRight)

    return SootNode(mock<DefinitionStmt> {
        on { it.leftOp } doReturn leftOp
        on { it.rightOp } doReturn rightOp
    })
}

/**
 * Creates a mock of a [Value] such that no such two mocks equal each other.
 */
fun mockTypedValue(valueType: Type? = null) =
    mock<Value> {
        on { it.type } doReturn (valueType ?: mock {})
    }
