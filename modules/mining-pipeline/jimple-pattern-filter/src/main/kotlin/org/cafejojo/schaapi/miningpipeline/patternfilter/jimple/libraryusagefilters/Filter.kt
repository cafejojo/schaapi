package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.libraryusagefilters

import soot.Body

/**
 * Represents a library usage filter.
 */
interface Filter {
    /**
     * Applies the filtering process.
     *
     * @param body method body
     */
    fun apply(body: Body)
}
