package org.cafejojo.schaapi.usagegraphgenerator.jimple

import org.cafejojo.schaapi.common.Node

/**
 * Creates a DOT graph of a control flow graph.
 *
 * @property name name of the graph.
 * @property cfg control flow graph.
 */
class DotGraphRenderer(private val name: String, private val cfg: Node) {
    private val result = StringBuilder()
    private val visited = HashSet<Node>()

    /**
     * Renders a control flow graph in DOT format.
     *
     * @return a control flow graph in DOT format.
     */
    fun render(): String {
        result.append("digraph \"$name()\" {\n")
        render(cfg)
        result.append("}")

        return result.toString()
    }

    private fun render(node: Node) {
        visited.add(node)

        result.append("    \"${node.hashCode()}\" [shape=ellipse, label=\"${getNodeLabel(node)}\"]\n")

        node.successors.forEach { successor ->
            result.append("    \"${node.hashCode()}\" -> \"${successor.hashCode()}\"\n")

            if (!visited.contains(successor)) render(successor)
        }
    }

    private fun getNodeLabel(node: Node) = node.toString().replace("\"", "^")
}
