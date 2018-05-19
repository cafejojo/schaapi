package org.cafejojo.schaapi.patternfilter

import org.cafejojo.schaapi.common.Node

/**
 * A subclass of [Node] for testing purposes.
 */
internal class TestNode(override val successors: MutableList<Node> = mutableListOf()) : Node