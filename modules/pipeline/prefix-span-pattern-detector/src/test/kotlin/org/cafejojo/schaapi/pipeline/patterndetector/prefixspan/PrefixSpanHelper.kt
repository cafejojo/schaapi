package org.cafejojo.schaapi.pipeline.patterndetector.prefixspan

/**
 * Calculates how many sub sequences a given sequence may have.
 */
fun amountOfPossibleSubSequences(sequenceLength: Int): Int = (0..sequenceLength).sum()
