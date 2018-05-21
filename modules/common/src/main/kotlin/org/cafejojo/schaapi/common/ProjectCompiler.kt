package org.cafejojo.schaapi.common

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
