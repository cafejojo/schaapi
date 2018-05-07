package org.cafejojo.schaapi.usagegraphgenerator

import org.cafejojo.schaapi.common.Node
import soot.Body
import soot.Unit
import soot.toolkits.graph.BriefUnitGraph
import soot.toolkits.graph.UnitGraph

/**
 * Creates the control flow graph of a method body.
 */
class ControlFlowGraphCreator(body: Body) {
    private val cfg: UnitGraph
    private val mappedUnits = HashMap<Unit, Node>()

    init {
        cfg = BriefUnitGraph(body)
    }

    /**
     * Wraps the control flow graph recursively within [Node] objects.
     *
     * @param unit the unit to wrap
     * @param predecessor the predecessor of the to be created [Node]
     * @return the [unit] wrapped within a [Node]
     */
    fun generate(unit: Unit = cfg.rootUnitIfExists(), predecessor: Node? = null): Node? {
        if (mappedUnits.containsKey(unit)) {
            mappedUnits[unit]?.let { predecessor?.successors?.add(it) }
            return mappedUnits[unit]
        }

        val node = SootNode(unit)

        predecessor?.successors?.add(node)

        mappedUnits[unit] = node

        cfg.getSuccsOf(unit).forEach { successor ->
            generate(successor, node)
        }

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
