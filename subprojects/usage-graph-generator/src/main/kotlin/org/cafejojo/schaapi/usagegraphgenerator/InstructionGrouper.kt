package org.cafejojo.schaapi.usagegraphgenerator

import de.codesourcery.asm.controlflow.ControlFlowGraph
import de.codesourcery.asm.controlflow.IBlock
import de.codesourcery.asm.controlflow.MethodEntry
import de.codesourcery.asm.controlflow.MethodExit
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.util.Printer

class InstructionGrouper(private val cfg: ControlFlowGraph) {
    private val visited = HashMap<IBlock, Node>()

    fun groupToStatements(block: IBlock = cfg.start, previous: Node? = null): Node? {
        if (visited.containsKey(block)) {
            visited[block]?.let { previous?.successors?.add(it) }
            return null
        }

        if (!isProcessableBlock(block)) {
            block.edges.filter { it.dst != block }.forEach({ groupToStatements(it.dst, previous) })

            return null
        }

        val (first, last) = when (block) {
            is MethodEntry -> EntryNode().let { Pair(it, it) }
            is MethodExit -> ExitNode().let { Pair(it, it) }
            else -> convertBlockToNode(block)
        }

        previous?.successors?.add(first)

        visited[block] = first

        block.edges.filter { it.dst != block }.forEach({ groupToStatements(it.dst, last) })

        return first
    }

    private fun isProcessableBlock(block: IBlock) = block is MethodEntry || block is MethodExit || !block.isVirtual(cfg.method)

    private fun convertBlockToNode(block: IBlock): Pair<Node, Node> {
        var instructionIndex = 0

        var first: InstructionsNode? = null
        var last: InstructionsNode? = null

        cfg.method.instructions.iterator().forEach { instruction ->
            if (!block.containsInstructionNum(instructionIndex++)) return@forEach

            if (instruction is LabelNode) {
                val current = StatementNode()
                last?.successors?.add(current)
                last = current
                if (first == null) first = last
            }

            if (instruction.opcode < 0 || instruction.opcode >= Printer.OPCODES.size) return@forEach

            if (last == null) throw IllegalStateException("Instruction found before label")

            last!!.instructions.add(instruction)
        }

        return Pair(first!!, last!!)
    }
}
