package org.cafejojo.schaapi.common

/**
 * A sequence of [Node]s.
 *
 * Consecutive [Node]s in a [Pattern] are not necessarily each other's [Node.successors], although implementations are
 * free to add such a restriction.
 */
typealias Pattern = List<Node>
