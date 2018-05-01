package org.cafejojo.schaapi.usagegraphgenerator

import org.objectweb.asm.tree.AbstractInsnNode
import java.util.UUID

/**
 * Represents a statement node.
 *
 * Contains references to the successor nodes.
 */
abstract class Node(val successors: MutableList<Node> = arrayListOf(), val id: NodeId = UuidNodeId()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

/**
 * Represents a non virtual statement node containing actual instructions.
 */
abstract class InstructionsNode(successors: MutableList<Node> = arrayListOf(), id: NodeId = UuidNodeId()) :
    Node(successors, id) {
    val instructions: MutableList<AbstractInsnNode> = arrayListOf()
}

/**
 * Represents the start of a method as a virtual statement node.
 */
class EntryNode(successors: MutableList<Node> = arrayListOf(), id: NodeId = UuidNodeId()) :
    Node(successors, id)

/**
 * Represents the end of a method as a virtual statement node.
 */
class ExitNode(successors: MutableList<Node> = arrayListOf(), id: NodeId = UuidNodeId()) :
    Node(successors, id)

/**
 * Represents a regular statement.
 */
class StatementNode(successors: MutableList<Node> = arrayListOf(), id: NodeId = UuidNodeId()) :
    InstructionsNode(successors, id)

/**
 * Represents a branching node.
 */
class BranchNode(successors: MutableList<Node> = arrayListOf(), id: NodeId = UuidNodeId()) :
    InstructionsNode(successors, id) {
    /**
     * Returns the successor of the 'positive' conditional branch.
     */
    fun trueSuccessor() = successors[1]

    /**
     * Returns the successor of the 'negative' conditional branch.
     */
    fun falseSuccessor() = successors[0]
}

/**
 * Represents a node id.
 */
interface NodeId

/**
 * Represents a [UUID] based node id.
 *
 * @property id [UUID] based node id.
 */
data class UuidNodeId(val id: UUID = UUID.randomUUID()) : NodeId

/**
 * Represents a customer integer based node id.
 *
 * @property id custom integer based node id.
 */
data class CustomNodeId(val id: Int) : NodeId
