package org.cafejojo.schaapi.usagegraphgenerator

import org.objectweb.asm.tree.AbstractInsnNode

abstract class Node {
    val successors: MutableList<Node> = arrayListOf()
}

abstract class InstructionsNode : Node() {
    val instructions: MutableList<AbstractInsnNode> = arrayListOf()
}

class EntryNode : Node()

class ExitNode : Node()

class StatementNode : InstructionsNode()

class BranchNode : InstructionsNode() {
    fun trueSuccessor() = successors[0]
    fun falseSuccessor() = successors[1]
}
