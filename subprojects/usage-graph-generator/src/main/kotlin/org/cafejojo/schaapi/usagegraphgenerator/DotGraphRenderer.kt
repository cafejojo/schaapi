package org.cafejojo.schaapi.usagegraphgenerator

import java.io.File

class DotGraphRenderer(private val name: String, private val scfg: Node) {
    val result = StringBuilder()
    private val visited = HashSet<Node>()

    fun render(): DotGraphRenderer {
        result.append("digraph \"$name()\" {\n")
        render(scfg)
        result.append("}")

        return this
    }

    fun write(fileName: String) = File(fileName).writeText(result.toString())

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
