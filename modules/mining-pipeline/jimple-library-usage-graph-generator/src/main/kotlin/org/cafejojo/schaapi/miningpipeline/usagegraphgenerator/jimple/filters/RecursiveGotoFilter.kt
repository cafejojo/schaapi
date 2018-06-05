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
        var repeat = true

        while (repeat) {
            repeat = false

            body.units.snapshotIterator().forEach {
                if (!retain(it)) {
                    body.units.remove(it)
                    repeat = true
                }
            }
        }
    }

    /**
     * True iff [unit] is not a [GotoStmt] of which the target is itself.
     *
     * @param unit a [Unit]
     */
    fun retain(unit: Unit) = unit !is GotoStmt || unit != unit.target
}
