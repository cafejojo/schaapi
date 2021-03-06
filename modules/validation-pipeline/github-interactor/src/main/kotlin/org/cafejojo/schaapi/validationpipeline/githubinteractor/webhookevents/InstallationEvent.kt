package org.cafejojo.schaapi.validationpipeline.githubinteractor.webhookevents

internal data class InstallationEvent(
    val installation: Installation,
    val action: String = "",
    val repositories: List<Repository>? = null
) {
    fun isCreated() = action == "created"
    fun isDeleted() = action == "deleted"
}
