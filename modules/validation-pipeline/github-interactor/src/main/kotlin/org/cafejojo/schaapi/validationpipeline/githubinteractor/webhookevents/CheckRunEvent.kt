package org.cafejojo.schaapi.validationpipeline.githubinteractor.webhookevents

internal data class CheckRunEvent(
    val installation: Installation,
    val action: String,
    val checkRun: CheckRun,
    val repository: Repository
) {
    fun isRerequested() = action == "rerequested"
}

internal data class CheckRun(val check_suite: CheckSuite)
