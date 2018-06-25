package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.processors

import soot.Body

/**
 * (Potentially) modifies a given method body.
 */
interface Processor {
    /**
     * (Potentially) modifies a [body].
     *
     * @param body the body to process
     */
    fun process(body: Body)
}
