package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import soot.RefType
import soot.Type
import soot.UnitPrinter
import soot.Value
import soot.ValueBox
import soot.util.Switch

/**
 * A [Value] that does not contain other values, and that implements only functionality related to equivalence.
 */
class SimpleValue(type: String) : RecursiveValue(type)

/**
 * A [Value] that may contain other values, and that implements only functionality related to equivalence.
 */
open class RecursiveValue(private val type: String, private vararg val subValues: Value) : Value {
    override fun equivTo(other: Any?) =
        other is RecursiveValue
            && this.type == other.type
            && this.subValues.size == other.subValues.size
            && this.subValues.zip(other.subValues).all { it.first.equivTo(it.second) }

    override fun getType(): Type = RefType.v(type)

    override fun apply(switch: Switch?) {
        throw NotImplementedError("`apply` is not implemented in `RecursiveValue`.")
    }

    override fun clone(): Any {
        throw NotImplementedError("`clone` is not implemented in `RecursiveValue`.")
    }

    @SuppressWarnings("ExceptionRaisedInUnexpectedLocation")
    override fun toString(up: UnitPrinter?) {
        throw NotImplementedError("`toString` is not implemented in `RecursiveValue`.")
    }

    override fun equivHashCode(): Int {
        throw NotImplementedError("`equivHashCode` is not implemented in `RecursiveValue`.")
    }

    override fun getUseBoxes(): MutableList<ValueBox> {
        throw NotImplementedError("`getUseBoxes` is not implemented in `RecursiveValue`.")
    }
}
