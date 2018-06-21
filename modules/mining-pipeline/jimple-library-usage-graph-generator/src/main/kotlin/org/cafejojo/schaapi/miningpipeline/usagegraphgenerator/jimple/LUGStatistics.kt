package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple

import org.cafejojo.schaapi.models.project.JavaProject

/**
 * Contains values that represent counts of findings.
 */
class LUGStatistics {
    /**
     * The total amount of concrete methods observed in user [JavaProject]s up until this point.
     */
    var concreteMethods = 0L
        internal set

    /**
     * The total amount of statements observed in user [JavaProject]s up until this point.
     */
    var statements = 0L
        internal set

    /**
     * The total amount of valid statements observed in user [JavaProject]s up until this point.
     */
    var validStatements = 0L
        internal set
}
