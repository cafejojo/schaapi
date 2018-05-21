package org.cafejojo.schaapi.pipeline

import org.cafejojo.schaapi.models.Project

/**
 * Compiles [Project]s.
 */
interface ProjectCompiler {
    /**
     * Compiles a [Project].
     *
     * There is no guarantee that the returned [Project] is the same instance as the given [project].
     *
     * @param project an uncompiled project
     * @return a compiled project
     */
    fun compile(project: Project): Project
}
