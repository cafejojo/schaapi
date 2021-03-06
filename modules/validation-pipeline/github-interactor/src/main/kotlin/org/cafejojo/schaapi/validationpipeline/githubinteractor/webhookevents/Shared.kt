package org.cafejojo.schaapi.validationpipeline.githubinteractor.webhookevents

internal data class Installation(val id: Int = 0, val account: Account? = null)

internal data class Repository(val name: String, val fullName: String, val private: Boolean)

internal data class Account(val login: String)

internal data class CheckSuite(val id: Int, val headBranch: String = "", val headSha: String = "")
