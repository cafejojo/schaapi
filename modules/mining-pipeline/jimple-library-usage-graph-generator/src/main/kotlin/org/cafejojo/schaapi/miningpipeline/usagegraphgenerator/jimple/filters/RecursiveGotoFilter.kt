package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters

import soot.Body
import soot.Unit
import soot.jimple.GotoStmt

/**
 * Removes recursive [GotoStmt]s.
 */
internal class RecursiveGotoFilter : Filter {
    /**
     * Removes recursive [GotoStmt]s.
     *
     * @param body a body
     */
    override fun apply(body: Body) {
        body.units.reversed().forEach {
            if (!retain(it)) body.units.remove(it)
        }

        if (body.units.size > 0) {
            val first = body.units.first
            if (!retain(first)) body.units.remove(first)
        }
    }

    /**
     * True iff [unit] is not a [GotoStmt] of which the target is itself.
     *
     * @param unit a [Unit]
     */
    fun retain(unit: Unit) = unit !is GotoStmt || unit != unit.target
}
