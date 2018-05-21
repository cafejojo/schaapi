package org.cafejojo.schaapi.pipeline.patternfilter.jimple

import org.cafejojo.schaapi.models.Node

/**
 * A subclass of [Node] for testing purposes.
 */
internal class TestNode(override val successors: MutableList<Node> = mutableListOf()) : Node
