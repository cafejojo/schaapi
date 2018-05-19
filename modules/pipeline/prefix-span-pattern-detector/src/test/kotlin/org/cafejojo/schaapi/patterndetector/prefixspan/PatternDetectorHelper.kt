package org.cafejojo.schaapi.patterndetector.prefixspan

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import soot.Type
import soot.Value
import soot.jimple.DefinitionStmt

/**
 * Create a Jimple node that returns a mock of a [Value] such that by default no two values have the same type.
 */
fun mockJimpleNode(valueLeft: Value, valueRight: Value): JimpleNode =
    JimpleNode(mock<DefinitionStmt> {
        on { it.leftOp } doReturn valueLeft
        on { it.rightOp } doReturn valueRight
    })

/**
 * Create a Jimple node that returns a mock of a [Value] such that by default no two values have the same type.
 */
fun mockJimpleNode(valueTypeLeft: Type? = null, valueTypeRight: Type? = null): JimpleNode =
    mockJimpleNode(mockTypedValue(valueTypeLeft), mockTypedValue(valueTypeRight))

/**
 * Creates a mock of a [Value] such that no such two mocks equal each other.
 */
fun mockTypedValue(valueType: Type? = null): Value = mock { on { it.type } doReturn (valueType ?: mock {}) }

/**
 * Calculate how many sub sequences a given sequence may have.
 */
fun amountOfPossibleSubSequences(sequenceLength: Int) = (0 .. sequenceLength).sum()
