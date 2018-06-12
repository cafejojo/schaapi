package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple

import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.PatternFilterRule
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleValueVisitor
import org.cafejojo.schaapi.models.project.JavaProject
import soot.Immediate
import soot.Value

/**
 * Filters out patterns that do not have enough calls to the library project.
 *
 * @param libraryProject the project that should be used
 * @property minUseCount the minimum number of usages per pattern to be qualified as a "sufficient user"
 */
class InsufficientLibraryUsageFilter(libraryProject: JavaProject, private val minUseCount: Int) :
    PatternFilterRule<JimpleNode> {
    private val libraryUsageVisitor = LibraryUsageVisitor(libraryProject)

    /**
     * Returns true iff there are at least [minUseCount] nodes in [pattern] that use classes from the library project.
     *
     * @param pattern the pattern to check for library usages
     * @return true iff there are at least [minUseCount] nodes in [pattern] that use classes from the library project
     */
    override fun retain(pattern: Pattern<JimpleNode>) =
        pattern.count { it.getTopLevelValues().any { libraryUsageVisitor.visit(it) } } >= minUseCount
}

/**
 * A visitor that determines whether a given [Value] uses [libraryProject].
 *
 * @property libraryProject the project that should be used
 */
class LibraryUsageVisitor(private val libraryProject: JavaProject) : JimpleValueVisitor<Boolean>() {
    /**
     * Returns false.
     *
     * @param value a [Value]
     * @return false
     */
    override fun defaultApply(value: Value) = false

    /**
     * Returns true iff the type of [value] is a library class.
     *
     * @param value an [Immediate]
     * @return true iff the type of [value] is a library class
     */
    override fun apply(value: Immediate) = isLibraryClass(value.type.toString())

    /**
     * Returns true iff [className] is a class defined in [libraryProject].
     *
     * @param className a class name
     * @return true iff [className] is a class defined in [libraryProject]
     */
    private fun isLibraryClass(className: String) = libraryProject.classNames.contains(className)

    /**
     * Returns true iff [result1] and [result2] are both true.
     *
     * @param result1 a [Boolean]
     * @param result2 a [Boolean]
     * @return true iff [result1] and [result2] are both true
     */
    override fun accumulate(result1: Boolean, result2: Boolean) = result1 && result2
}
