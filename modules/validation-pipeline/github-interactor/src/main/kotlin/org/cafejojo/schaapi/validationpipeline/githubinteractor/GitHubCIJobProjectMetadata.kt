package org.cafejojo.schaapi.validationpipeline.githubinteractor

import org.cafejojo.schaapi.validationpipeline.CIJobProjectMetadata

/**
 * Metadata of a GitHub project for a specific CI job run.
 */
data class GitHubCIJobProjectMetadata(val checkRunId: Int, val installationId: Int, val fullName: String) :
    CIJobProjectMetadata {
    override fun getIdentifier() = checkRunId.toString()
}
