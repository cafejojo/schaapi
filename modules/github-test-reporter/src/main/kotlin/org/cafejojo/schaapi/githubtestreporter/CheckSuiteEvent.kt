package org.cafejojo.schaapi.githubtestreporter

internal data class CheckSuiteEvent(
    val installation: Installation,
    val action: String = "",
    val checkSuite: CheckSuite
)

internal data class Installation(val id: Int = 0)

internal data class App(
    val externalUrl: String,
    val updatedAt: Int,
    val htmlUrl: String = "",
    val name: String = "",
    val description: String = "",
    val createdAt: Int = 0,
    val id: Int = 0
)

internal data class CheckSuite(
    val app: App,
    val headCommit: HeadCommit,
    val headBranch: String = "",
    val before: String = "",
    val createdAt: String = "",
    val headSha: String = "",
    val url: String = "",
    val conclusion: String?,
    val updatedAt: String = "",
    val uniqueCheckRunsCount: Int = 0,
    val id: Int = 0,
    val after: String = "",
    val checkRunsUrl: String = "",
    val status: String = ""
)

internal data class Author(
    val name: String = "",
    val email: String = ""
)

internal data class HeadCommit(
    val treeId: String = "",
    val author: Author,
    val id: String = "",
    val message: String = "",
    val timestamp: String = ""
)
