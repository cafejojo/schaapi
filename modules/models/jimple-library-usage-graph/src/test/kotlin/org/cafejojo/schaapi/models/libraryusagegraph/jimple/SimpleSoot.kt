package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import soot.RefType
import soot.SootMethod
import soot.Value
import soot.jimple.InvokeExpr
import soot.jimple.internal.AbstractBinopExpr
import soot.jimple.internal.AbstractNegExpr
import soot.jimple.internal.AbstractSpecialInvokeExpr
import soot.util.Switch

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
 * An [InvokeExpr] that does not contain other values, and that implements only functionality related to equivalence.
 */
fun mockInvokeExpr(type: String): InvokeExpr = mock {
    on { it.type } doReturn RefType.v(type)
    on { it.equivTo(any()) } doAnswer { answer ->
        val other = answer.arguments[0]

        other is InvokeExpr && it.type == other.type
    }
    on { it.equivHashCode() } doReturn type.hashCode()
}

/**
 * A simple implementation of [SootMethod].
 */
class SimpleSootMethod(name: String, parameterTypes: List<String>, returnType: String) :
    SootMethod(name, parameterTypes.map { RefType.v(it) }, RefType.v(returnType))

/**
 * A simple implementation of [soot.jimple.internal.AbstractUnopExpr].
 */
class SimpleUnopExpr(value: Value) : AbstractNegExpr(mockValueBox(value)) {
    /**
     * Creates a [SimpleUnopExpr] containing a [Value] of the given type.
     */
    constructor(type: String) : this(mockValue(type))

    override fun clone() = throw NotImplementedError("The called method is not implemented.")
}

/**
 * A simple implementation of [soot.jimple.internal.AbstractBinopExpr].
 */
class SimpleBinopExpr(leftValue: Value, rightValue: Value) : AbstractBinopExpr() {
    init {
        op1Box = mockValueBox(leftValue)
        op2Box = mockValueBox(rightValue)
    }

    constructor(leftType: String, rightType: String) : this(mockValue(leftType), mockValue(rightType))

    constructor(leftValue: Value, rightType: String) : this(leftValue, mockValue(rightType))

    constructor(leftType: String, rightValue: Value) : this(mockValue(leftType), rightValue)

    override fun getSymbol() = "+"

    override fun getType() = throw NotImplementedError("The called method is not implemented.")

    override fun apply(switch: Switch?) = throw NotImplementedError("The called method is not implemented.")

    override fun clone() = throw NotImplementedError("The called method is not implemented.")
}

/**
 * A simple implementation of [soot.jimple.internal.AbstractInvokeExpr].
 */
@SuppressWarnings("SpreadOperator") // No clean alternative
class SimpleInvokeExpr(base: Value, sootMethod: SootMethod, vararg arguments: Value) :
    AbstractSpecialInvokeExpr(
        mockValueBox(base),
        mockSootMethodRef(sootMethod),
        arguments.map { mockValueBox(it) }.toTypedArray()
    ) {
    constructor(base: String, sootMethod: SootMethod, vararg arguments: String)
        : this(mockValue(base), sootMethod, *arguments.map { mockValue(it) }.toTypedArray())

    constructor(base: String, sootMethod: SootMethod, vararg arguments: Value)
        : this(mockValue(base), sootMethod, *arguments)

    constructor(base: Value, sootMethod: SootMethod, vararg arguments: String)
        : this(base, sootMethod, *arguments.map { mockValue(it) }.toTypedArray())

    override fun clone() = throw NotImplementedError("The called method is not implemented.")
}
