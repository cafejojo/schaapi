package org.cafejojo.schaapi.pipeline.projectcompiler.githubminer

import org.cafejojo.schaapi.models.Project
import org.cafejojo.schaapi.pipeline.CodeMiner

/**
 * Mine Github for code.
 */
class CodeMiner : CodeMiner {
    override fun mine(groupId: String, artifactId: String): List<Project> = emptyList()
}
