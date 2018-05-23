package org.cafejojo.schaapi.pipeline.patterndetector.prefixspan

import soot.RefType
import soot.Type
import soot.UnitPrinter
import soot.Value
import soot.util.Switch
import java.util.UUID

/**
 * A simple implementation of [Value] that implements only the functionality for comparing [Value]s.
 */
open class EmptyValue(private val type: String) : Value {
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
 * A simple implementation of [Value] that is not equal or equivalent to any other [Value].
 */
class UniqueValue : EmptyValue(UUID.randomUUID().toString())
