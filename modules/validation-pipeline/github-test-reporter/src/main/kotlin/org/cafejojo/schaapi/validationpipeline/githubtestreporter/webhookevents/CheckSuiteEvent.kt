package org.cafejojo.schaapi.validationpipeline.githubtestreporter.webhookevents

internal data class CheckSuiteEvent(
    val installation: Installation,
    val action: String = "",
    val checkSuite: CheckSuite,
    val repository: Repository
) {
    fun isRequested() = action == "requested"
}

internal data class CheckSuite(val headBranch: String = "", val headSha: String = "")
