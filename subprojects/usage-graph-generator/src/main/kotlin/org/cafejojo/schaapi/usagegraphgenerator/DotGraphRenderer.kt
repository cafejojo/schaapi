package org.cafejojo.schaapi.usagegraphgenerator

/**
 * Creates DOT graph files of statement control flow graphs.
 *
 * @property name name of the graph.
 * @property scfg statement control flow graph.
 */
class DotGraphRenderer(private val name: String, private val scfg: Node) {
    private val result = StringBuilder()
    private val visited = HashSet<Node>()

    /**
     * Renders a scfg in DOT format.
     *
     * @return a scfg in DOT format.
     */
    fun render(): String {
        result.append("digraph \"$name()\" {\n")
        render(scfg)
        result.append("}")

        return result.toString()
    }

    private fun render(scfg: Node) {
        visited.add(scfg)

        result.append("    \"${sanitizeId(scfg.id)}\" [")
        result.append(
            when (scfg) {
                is EntryNode -> "shape=ellipse, label=method_entry"
                is ExitNode -> "shape=ellipse, label=method_exit"
                is StatementNode -> "shape=box, label=statement"
                is BranchNode -> "shape=box, label=branch"
                else -> "shape=box, label=UNKNOWN"
            }
        )
        result.append("]\n")

        scfg.successors.forEach { successor ->
            result.append("    \"${sanitizeId(scfg.id)}\" -> \"${sanitizeId(successor.id)}\"\n")

            if (!visited.contains(successor)) render(successor)
        }
    }

    private fun sanitizeId(id: NodeId) = id.toString().replace("-", "")
}
