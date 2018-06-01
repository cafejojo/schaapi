package org.cafejojo.schaapi.miningpipeline.miner.github

import org.cafejojo.schaapi.models.Project
import java.io.File

internal class TestProject(override val projectDir: File) : Project

internal fun testProjectPacker(projectDir: File) = TestProject(projectDir)
