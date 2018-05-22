package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import soot.RefType
import soot.Type
import soot.UnitPrinter
import soot.Value
import soot.ValueBox
import soot.util.Switch

class SimpleValue(private val type: String) : Value {
    override fun equivTo(other: Any?): Boolean {
        return other is SimpleValue && this.type == other.type
    }

    override fun getType(): Type {
        return RefType.v(type)
    }

    override fun apply(switch: Switch?) {
        throw NotImplementedError("`apply` is not implemented in `SimpleValue`.")
    }

    override fun clone(): Any {
        throw NotImplementedError("`clone` is not implemented in `SimpleValue`.")
    }

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
