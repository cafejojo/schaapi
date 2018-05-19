package org.cafejojo.schaapi.usagegraphgenerator

import org.cafejojo.schaapi.common.Node
import soot.Body
import soot.Unit
import soot.toolkits.graph.BriefUnitGraph
import soot.toolkits.graph.UnitGraph

/**
 * Creates the control flow graph of a method body.
 */
object ControlFlowGraphCreator {
    /**
     * Creates the control flow graph of a method body.
     *
     * @param body a Soot method [Body]
     * @return the [Body]'s root [Unit] wrapped in a [Node]
     */
    fun create(body: Body): Node? = BriefUnitGraph(body).let { transform(it, HashMap(), it.rootUnitIfExists()) }

    /**
     * Wraps the control flow graph recursively within [Node] objects.
     *
     * @param cfg the Soot control flow graph
     * @param mappedUnits visited units
     * @param unit the unit to wrap
     * @param predecessor the predecessor of the [Node] to be created
     * @return [unit] wrapped within a [Node]
     */
    private fun transform(
        cfg: UnitGraph,
        mappedUnits: HashMap<Unit, Node>,
        unit: Unit,
        predecessor: Node? = null
    ): Node? {
        if (mappedUnits.containsKey(unit)) {
            mappedUnits[unit]?.let { predecessor?.successors?.add(it) }
            return mappedUnits[unit]
        }

        val node = SootNode(unit)

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
fun UnitGraph.rootUnitIfExists() = if (heads.isEmpty()) throw NoRootInControlFlowGraphException() else heads[0]

/**
 * Exception for control flow graphs that have no root [Unit].
 */
class NoRootInControlFlowGraphException : Exception("The control flow graph does not contain a root unit.")
