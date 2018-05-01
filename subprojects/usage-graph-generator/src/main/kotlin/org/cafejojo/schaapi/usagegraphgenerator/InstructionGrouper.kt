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

        var instructionIndex = 0
        cfg.method.instructions.iterator().forEach { instruction ->
            if (!block.containsInstructionNum(instructionIndex++)) return@forEach
            println("${instructionIndex - 1}  $instruction")
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

    private fun convertBlockToNode(block: IBlock): Pair<Node, Node> {
        var instructionIndex = 0

        var first: InstructionsNode? = null
        var last: InstructionsNode? = null

        val lastLabel = findLastLabelInBlock(block)

        cfg.method.instructions.iterator().forEach { instruction ->
            if (!block.containsInstructionNum(instructionIndex++)) return@forEach

            if (instruction is LabelNode) {
                val current = if (instruction == lastLabel && isBranchBlock(block)) BranchNode() else StatementNode()
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

    private fun findLastLabelInBlock(block: IBlock): LabelNode? {
        var instructionIndex = 0
        var labelNode: LabelNode? = null
        cfg.method.instructions.iterator().forEach { instruction ->
            if (!block.containsInstructionNum(instructionIndex++)) return@forEach
            if (instruction is LabelNode) labelNode = instruction
        }
        return labelNode
    }

    private fun isProcessableBlock(block: IBlock) =
        block is MethodEntry || block is MethodExit || !block.isVirtual(cfg.method)

    private fun isBranchBlock(block: IBlock) = block.edges.size == 3 && block.edges.count { it.src == block } == 2
}
