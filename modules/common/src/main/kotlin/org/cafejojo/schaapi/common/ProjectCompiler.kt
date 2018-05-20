package org.cafejojo.schaapi.common

/**
 * Represents a project compiler.
 */
interface ProjectCompiler {
    /**
     * Compiles a project.
     *
     * @param project an uncompiled project
     * @return a compiled project
     */
    fun compile(project: Project): Project
}
