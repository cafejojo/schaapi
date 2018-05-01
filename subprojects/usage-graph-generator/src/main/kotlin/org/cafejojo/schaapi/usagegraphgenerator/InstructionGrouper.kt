package org.cafejojo.schaapi.usagegraphgenerator

import de.codesourcery.asm.controlflow.ControlFlowGraph
import de.codesourcery.asm.controlflow.IBlock
import de.codesourcery.asm.controlflow.MethodEntry
import de.codesourcery.asm.controlflow.MethodExit
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.util.Printer

/**
 * Groups instructions within CFG blocks into statement nodes that still represent the control flow.
 *
 * @property cfg the control flow graph to operate on.
 */
class InstructionGrouper(private val cfg: ControlFlowGraph) {
    private val visited = HashMap<IBlock, Node>()

    /**
     * Groups instructions within CFG [block]s into statement nodes that still represent the control flow.
     *
     * Each block is visited and converted to statement nodes. Blocks are explicitly visited only once,
     * to make sure 'merging' blocks do not get visited multiple times. Once blocks have been merged,
     * the new node is set as successor of the predecessor node.
     *
     * @param block CFG block.
     * @param predecessor predecessor for newly created statement nodes.
     * @return method entry node.
     */
    fun groupToStatements(block: IBlock = cfg.start, predecessor: Node? = null): Node? {
        if (visited.containsKey(block)) {
            visited[block]?.let { predecessor?.successors?.add(it) }
            return null
        }

        if (!isProcessableBlock(block)) {
            block.edges.filter { it.dst != block }.forEach({ groupToStatements(it.dst, predecessor) })
            return null
        }

        val (first, last) = when (block) {
            is MethodEntry -> EntryNode().let { Pair(it, it) }
            is MethodExit -> ExitNode().let { Pair(it, it) }
            else -> convertBlockToNodes(block)
        }

        predecessor?.successors?.add(first)

        visited[block] = first

        block.edges.filter { it.dst != block }.forEach({ groupToStatements(it.dst, last) })

        return first
    }

    /**
     * Converts a CFG [block] to statement nodes.
     *
     * The assumption is made that statements are separated by label nodes. If the label is the last label
     * within a branching block, a branch node is created instead of a statement node. All consequent
     * instructions will be added to the created node, until the next label node is encountered.
     *
     * @param block CFG block.
     * @return respectively the node representing the first and the node representing the last statement in the block.
     */
    private fun convertBlockToNodes(block: IBlock): Pair<Node, Node> {
        var instructionIndex = 0

        var first: InstructionsNode? = null
        var last: InstructionsNode? = null

        val lastLabel = findLastLabelInBlock(block)

        cfg.method.instructions.iterator().forEach(fun(instruction: AbstractInsnNode) {
            if (!block.containsInstructionNum(instructionIndex++)) return

            if (instruction is LabelNode) {
                val current = if (instruction == lastLabel && isBranchBlock(block)) BranchNode() else StatementNode()
                last?.successors?.add(current)
                last = current
                if (first == null) first = last
            }

            if (instruction.opcode < 0 || instruction.opcode >= Printer.OPCODES.size) return

            if (last == null) throw IllegalStateException("Instruction found before label")

            last?.instructions?.add(instruction)
        })

        val firstResult = first
        val lastResult = last
        if (firstResult == null || lastResult == null) throw IllegalStateException("Block contains no statements")

        return Pair(firstResult, lastResult)
    }

    /**
     * Finds the last label within the block.
     *
     * @param block CFG block.
     * @return last label within the given block.
     */
    private fun findLastLabelInBlock(block: IBlock): LabelNode? {
        var instructionIndex = 0
        var labelNode: LabelNode? = null
        cfg.method.instructions.iterator().forEach {
            if (block.containsInstructionNum(instructionIndex++) && it is LabelNode) labelNode = it
        }
        return labelNode
    }

    /**
     * Determines if the [block] under evaluation is relevant.
     *
     * @param block CFG block.
     * @return boolean indicating if the [block] under evaluation is relevant.
     */
    private fun isProcessableBlock(block: IBlock) =
        block is MethodEntry || block is MethodExit || !block.isVirtual(cfg.method)

    /**
     * Determines if the given [block] is a branching block.
     *
     * @param block CFG block.
     * @return boolean indicating if the given [block] is a branching block.
     */
    private fun isBranchBlock(block: IBlock) = block.edges.size == 3 && block.edges.count { it.src == block } == 2
}
