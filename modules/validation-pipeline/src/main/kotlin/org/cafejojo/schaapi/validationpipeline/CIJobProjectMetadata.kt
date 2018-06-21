package org.cafejojo.schaapi.validationpipeline

/**
 * Metadata of a project for a specific CI job run.
 */
interface CIJobProjectMetadata {
    /**
     * Returns a unique identifier for the current run.
     */
    fun getIdentifier(): String
}
