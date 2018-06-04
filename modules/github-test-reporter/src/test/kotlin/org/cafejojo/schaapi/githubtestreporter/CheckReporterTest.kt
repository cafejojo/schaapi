package org.cafejojo.schaapi.githubtestreporter

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it

object CheckReporterTest : Spek({
    it("can report the start of a check") {
        val checkReporter = CheckReporter()


        checkReporter.reportStarted(
            installationId = 123,
            owner = "cafejojo",
            repository = "schaapi",
            headBranch = "patch-1",
            headSha = "e02b431ab90e3e0f7301a6ca82503fd7f6cf159e"
        )

    }
})
