package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import soot.RefType
import soot.SootMethodRef
import soot.Type
import soot.UnitPrinter
import soot.Value
import soot.jimple.InvokeExpr
import soot.util.Switch

/**
 * A [Value] that does not contain other values, and that implements only functionality related to equivalence.
 */
class SimpleValue(private val type: String) : Value {
    override fun equivTo(other: Any?) = other is SimpleValue && this.type == other.type

    override fun getType(): Type = RefType.v(type)

    override fun apply(switch: Switch?) = throw NotImplementedError("The called method is not implemented.")

    override fun clone() = throw NotImplementedError("The called method is not implemented.")

    @SuppressWarnings("ExceptionRaisedInUnexpectedLocation")
    override fun toString(up: UnitPrinter?) = throw NotImplementedError("The called method is not implemented.")

    override fun equivHashCode() = throw NotImplementedError("The called method is not implemented.")

    override fun getUseBoxes() = throw NotImplementedError("The called method is not implemented.")
}

/**
 * An [InvokeExpr] that does not contain other values, and that implements only functionality related to equivalence.
 */
class SimpleInvokeExpr(private val type: String) : InvokeExpr {
    override fun getType(): Type = RefType.v(type)

    override fun equivTo(other: Any?) = other is SimpleInvokeExpr && this.type == other.type

    override fun getMethodRef() = throw NotImplementedError("The called method is not implemented.")

    override fun getArgs() = throw NotImplementedError("The called method is not implemented.")

    @SuppressWarnings("ExceptionRaisedInUnexpectedLocation")
    override fun toString(up: UnitPrinter?) = throw NotImplementedError("The called method is not implemented.")

    override fun equivHashCode() = throw NotImplementedError("The called method is not implemented.")

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
