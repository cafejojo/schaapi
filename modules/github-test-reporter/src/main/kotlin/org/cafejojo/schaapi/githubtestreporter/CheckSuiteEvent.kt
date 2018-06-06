package org.cafejojo.schaapi.githubtestreporter

internal data class CheckSuiteEvent(
    val installation: Installation,
    val action: String = "",
    val checkSuite: CheckSuite,
    val repository: Repository
) {
    fun isRequested() = action == "requested"
}

internal data class Installation(val id: Int = 0)

internal data class CheckSuite(val headBranch: String = "", val headSha: String = "")

internal data class Repository(val name: String, val owner: Owner)

internal data class Owner(val login: String)
