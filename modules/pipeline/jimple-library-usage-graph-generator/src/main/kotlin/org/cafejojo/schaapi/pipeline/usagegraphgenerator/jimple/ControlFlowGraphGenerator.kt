package org.cafejojo.schaapi.pipeline.usagegraphgenerator.jimple

import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import soot.Body
import soot.jimple.Stmt
import soot.toolkits.graph.BriefUnitGraph
import soot.toolkits.graph.UnitGraph
import soot.Unit as SootUnit

/**
 * Creates the control flow graph of a method body.
 */
internal object ControlFlowGraphGenerator {
    /**
     * Creates the control flow graph of a method body.
     *
     * @param body a Soot method [Body]
     * @return the [Body]'s root [Stmt] wrapped in a [JimpleNode]
     */
    internal fun create(body: Body): JimpleNode? =
        BriefUnitGraph(body).let { transform(it, HashMap(), it.rootUnitIfExists()) }

    /**
     * Wraps the control flow graph recursively within [JimpleNode] objects.
     *
     * @param cfg the Soot control flow graph
     * @param mappedUnits visited units
     * @param unit the unit to wrap
     * @param predecessor the predecessor of the [JimpleNode] to be created
     * @return [unit] wrapped within a [JimpleNode]
     */
    private fun transform(
        cfg: UnitGraph,
        mappedUnits: HashMap<SootUnit, JimpleNode>,
        unit: SootUnit,
        predecessor: JimpleNode? = null
    ): JimpleNode? {
        if (mappedUnits.containsKey(unit)) {
            mappedUnits[unit]?.let { predecessor?.successors?.add(it) }
            return mappedUnits[unit]
        }

        if (unit !is Stmt) throw IllegalArgumentException("Unit must be a statement.")

        val node = JimpleNode(unit)

        predecessor?.successors?.add(node)

        mappedUnits[unit] = node

        cfg.getSuccsOf(unit).forEach { transform(cfg, mappedUnits, it, node) }

        return node
    }
}

/**
 * Returns the root of the control flow graph if it exists, otherwise an exception is thrown.
 *
 * @return the root of the control flow graph.
 */
private fun UnitGraph.rootUnitIfExists(): SootUnit =
    if (heads.isEmpty()) throw NoRootInControlFlowGraphException() else heads[0]

/**
 * Exception for control flow graphs that have no root [SootUnit].
 */
private class NoRootInControlFlowGraphException : Exception("The control flow graph does not contain a root unit.")
