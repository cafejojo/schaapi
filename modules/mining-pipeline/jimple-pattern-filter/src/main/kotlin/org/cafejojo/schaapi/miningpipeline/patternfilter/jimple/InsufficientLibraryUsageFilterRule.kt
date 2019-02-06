package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple

import org.cafejojo.schaapi.miningpipeline.Pattern
import org.cafejojo.schaapi.miningpipeline.PatternFilterRule
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleValueVisitor
import org.cafejojo.schaapi.models.project.JavaProject
import soot.Value
import soot.jimple.StaticInvokeExpr
import soot.jimple.internal.JStaticInvokeExpr

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
        pattern.count { node -> node.getTopLevelValues().any { libraryUsageVisitor.visit(it) } } >= minUseCount
}

/**
 * A visitor that determines whether a given [Value] uses [libraryProject].
 *
 * @property libraryProject the project that should be used
 */
class LibraryUsageVisitor(private val libraryProject: JavaProject) : JimpleValueVisitor<Boolean>() {
    /**
     * Returns true iff the type of [value] is for a library class.
     *
     * @param value a [Value]
     * @return true iff the type of [value] is for a library class
     */
    override fun applyDefault(value: Value) = isLibraryClass(value.type.toString())

    /**
     * Returns true iff [JStaticInvokeExpr] declaring class is for a library.
     *
     * @param value a [StaticInvokeExpr]
     * @return true iff [JStaticInvokeExpr] declaring class is for a library
     */
    override fun apply(value: StaticInvokeExpr) = isLibraryClass(value.methodRef.declaringClass().toString())

    /**
     * Returns true iff [result1] or [result2] is true.
     *
     * @param result1 a [Boolean]
     * @param result2 a [Boolean]
     * @return true iff [result1] or [result2] is true
     */
    override fun accumulate(result1: Boolean, result2: Boolean) = result1 || result2

    /**
     * Returns true iff [className] is a class defined in [libraryProject].
     *
     * @param className a class name
     * @return true iff [className] is a class defined in [libraryProject]
     */
    private fun isLibraryClass(className: String) = libraryProject.classNames.contains(className)
}
