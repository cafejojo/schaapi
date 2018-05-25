package org.cafejojo.schaapi.pipeline.githubminer.mavenproject

import org.cafejojo.schaapi.models.Project
import java.io.File

class TestProject(override val projectDir: File) : Project

fun testProjectPacker(projectDir: File) = TestProject(projectDir)
