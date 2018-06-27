package org.cafejojo.schaapi.validationpipeline.githubinteractor.webhookevents

internal data class CheckSuiteEvent(
    val installation: Installation,
    val action: String = "",
    val checkSuite: CheckSuite,
    val repository: Repository
) {
    fun isRequested() = action == "requested"
}
