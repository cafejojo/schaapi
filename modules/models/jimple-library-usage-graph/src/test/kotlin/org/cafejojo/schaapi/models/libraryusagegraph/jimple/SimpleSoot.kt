package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import soot.RefType
import soot.SootMethod
import soot.SootMethodRef
import soot.Type
import soot.UnitPrinter
import soot.Value
import soot.jimple.InvokeExpr
import soot.jimple.internal.AbstractBinopExpr
import soot.jimple.internal.AbstractNegExpr
import soot.jimple.internal.AbstractSpecialInvokeExpr
import soot.util.Switch

/**
 * A [Value] that does not contain other values, and that implements only functionality related to equivalence.
 */
class EmptyValue(private val type: String) : Value {
    override fun getType(): Type = RefType.v(type)

    override fun equivTo(other: Any?) = other is EmptyValue && this.type == other.type

    override fun equivHashCode() = type.hashCode()

    override fun apply(switch: Switch?) = throw NotImplementedError("The called method is not implemented.")

    override fun clone() = throw NotImplementedError("The called method is not implemented.")

    @SuppressWarnings("ExceptionRaisedInUnexpectedLocation")
    override fun toString(up: UnitPrinter?) = throw NotImplementedError("The called method is not implemented.")

    override fun getUseBoxes() = throw NotImplementedError("The called method is not implemented.")
}

/**
 * An [InvokeExpr] that does not contain other values, and that implements only functionality related to equivalence.
 */
class EmptyInvokeExpr(private val type: String) : InvokeExpr {
    override fun getType(): Type = RefType.v(type)

    override fun equivTo(other: Any?) = other is EmptyInvokeExpr && this.type == other.type

    override fun equivHashCode() = type.hashCode()

    override fun getMethodRef() = throw NotImplementedError("The called method is not implemented.")

    override fun getArgs() = throw NotImplementedError("The called method is not implemented.")

    @SuppressWarnings("ExceptionRaisedInUnexpectedLocation")
    override fun toString(up: UnitPrinter?) = throw NotImplementedError("The called method is not implemented.")

    override fun getMethod() = throw NotImplementedError("The called method is not implemented.")

    override fun getArgBox(index: Int) = throw NotImplementedError("The called method is not implemented.")

    override fun setArg(index: Int, arg: Value?) = throw NotImplementedError("The called method is not implemented.")

    override fun apply(switch: Switch?) = throw NotImplementedError("The called method is not implemented.")

    override fun clone() = throw NotImplementedError("The called method is not implemented.")

    override fun getArgCount() = throw NotImplementedError("The called method is not implemented.")

    override fun getUseBoxes() = throw NotImplementedError("The called method is not implemented.")

    override fun setMethodRef(smr: SootMethodRef?) = throw NotImplementedError("The called method is not implemented.")

    override fun getArg(index: Int) = throw NotImplementedError("The called method is not implemented.")
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
     * Creates a [SimpleUnopExpr] containing an [EmptyValue] of the given type.
     */
    constructor(type: String) : this(EmptyValue(type))

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

    /**
     * Creates a [SimpleBinopExpr] containing [EmptyValue]s of the given types.
     */
    constructor(leftType: String, rightType: String) : this(EmptyValue(leftType), EmptyValue(rightType))

    constructor(leftValue: Value, rightType: String) : this(leftValue, EmptyValue(rightType))

    constructor(leftType: String, rightValue: Value) : this(EmptyValue(leftType), rightValue)

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
        : this(EmptyValue(base), sootMethod, *arguments.map { EmptyValue(it) }.toTypedArray())

    constructor(base: String, sootMethod: SootMethod, vararg arguments: Value)
        : this(EmptyValue(base), sootMethod, *arguments)

    constructor(base: Value, sootMethod: SootMethod, vararg arguments: String)
        : this(base, sootMethod, *arguments.map { EmptyValue(it) }.toTypedArray())

    override fun clone() = throw NotImplementedError("The called method is not implemented.")
}
