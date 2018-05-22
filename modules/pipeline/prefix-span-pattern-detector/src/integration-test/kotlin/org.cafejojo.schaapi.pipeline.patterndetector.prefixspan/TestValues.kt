package org.cafejojo.schaapi.pipeline.patterndetector.prefixspan

import soot.RefType
import soot.Type
import soot.UnitPrinter
import soot.Value
import soot.ValueBox
import soot.util.Switch
import java.util.UUID

/**
 * A simple implementation of [Value] that implements only the functionality for comparing [Value]s.
 */
open class SimpleValue(private val type: String) : Value {
    override fun equivTo(other: Any?) = other is SimpleValue && this.type == other.type

    override fun getType(): Type = RefType.v(type)

    override fun apply(switch: Switch?) {
        throw NotImplementedError("`apply` is not implemented in `SimpleValue`.")
    }

    override fun clone(): Any {
        throw NotImplementedError("`clone` is not implemented in `SimpleValue`.")
    }

    @SuppressWarnings("ExceptionRaisedInUnexpectedLocation")
    override fun toString(up: UnitPrinter?) {
        throw NotImplementedError("`toString` is not implemented in `SimpleValue`.")
    }

    override fun equivHashCode(): Int {
        throw NotImplementedError("`equivHashCode` is not implemented in `SimpleValue`.")
    }

    override fun getUseBoxes(): MutableList<ValueBox> {
        throw NotImplementedError("`getUseBoxes` is not implemented in `SimpleValue`.")
    }
}

/**
 * A simple implementation of [Value] that is not equal or equivalent to any other [Value].
 */
class UniqueValue : SimpleValue(UUID.randomUUID().toString())
