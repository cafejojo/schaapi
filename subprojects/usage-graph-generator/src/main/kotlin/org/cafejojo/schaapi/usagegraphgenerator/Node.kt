package org.cafejojo.schaapi.usagegraphgenerator

import org.objectweb.asm.tree.AbstractInsnNode
import java.util.UUID

/**
 * Represents a statement node.
 *
 * Contains references to the successor nodes.
 */
abstract class Node(val id: UUID = UUID.randomUUID()) {
    val successors: MutableList<Node> = arrayListOf()
}

/**
 * Represents a non virtual statement nodes containing actual instructions.
 */
abstract class InstructionsNode : Node() {
    val instructions: MutableList<AbstractInsnNode> = arrayListOf()
}

/**
 * Represents the start of a method as a virtual statement node.
 */
class EntryNode : Node()

/**
 * Represents the end of a method as a virtual statement node.
 */
class ExitNode : Node()

/**
 * Represents a regular statement.
 */
class StatementNode : InstructionsNode()

/**
 * Represents a branching node.
 */
class BranchNode : InstructionsNode() {
    /**
     * Return the successor of the 'positive' conditional branch.
     */
    fun trueSuccessor() = successors[1]

    /**
     * Return the successor of the 'negative' conditional branch.
     */
    fun falseSuccessor() = successors[0]
}
