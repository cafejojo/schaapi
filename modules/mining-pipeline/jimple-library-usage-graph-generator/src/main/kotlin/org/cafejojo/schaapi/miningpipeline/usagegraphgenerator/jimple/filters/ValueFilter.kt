package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters

import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleValueVisitor
import org.cafejojo.schaapi.models.project.JavaProject
import soot.EquivalentValue
import soot.PrimType
import soot.Scene
import soot.SootClass
import soot.Type
import soot.Value
import soot.jimple.CastExpr
import soot.jimple.Constant
import soot.jimple.DynamicInvokeExpr
import soot.jimple.IdentityRef
import soot.jimple.InstanceFieldRef
import soot.jimple.InstanceInvokeExpr
import soot.jimple.InstanceOfExpr
import soot.jimple.NewArrayExpr
import soot.jimple.NewExpr
import soot.jimple.NewMultiArrayExpr
import soot.jimple.StaticFieldRef
import soot.jimple.StaticInvokeExpr
import soot.jimple.toolkits.infoflow.AbstractDataSource
import soot.jimple.toolkits.thread.synchronization.NewStaticLock

/**
 * Determines whether to keep individual [Value]s.
 */
internal class ValueFilter(libraryProject: JavaProject) {
    private val filterRules = listOf(
        ClassValueFilterRule(),
        LibraryUsageValueFilterRule(libraryProject),
        UserUsageValueFilterRule(libraryProject)
    )

    /**
     * Returns true iff [value] is of a useful class (as determined by [ClassValueFilterRule]), has a library usage, and
     * does not have any user project usages.
     *
     * @param value a [Value]
     * @return true iff [value] is of a useful class (as determined by [ClassValueFilterRule]), has a library usage, and
     * does not have any user project usages
     */
    fun retain(value: Value) = filterRules.all { it.retain(value) }

    /**
     * Returns true iff all [values] are of a useful class (as determined by [ClassValueFilterRule]), at least one of them
     * has a library usage, and none of them has a user project usage.
     *
     * @param values a collection of [Value]s
     * @return true iff all [values] are of a useful class (as determined by [ClassValueFilterRule]), at least one of them
     * has a library usage, and none of them has a user project usage
     */
    fun retain(values: Iterable<Value>) = filterRules.all { it.retain(values) }
}

/**
 * Describes how a [Value] should be filtered.
 */
private abstract class ValueFilterRule : JimpleValueVisitor<Boolean>() {
    /**
     * Returns true iff [value] should be retained.
     *
     * @param value a [Value]
     * @return true iff [value] should be retained
     */
    fun retain(value: Value) = visit(value)

    /**
     * Returns true iff all [values] should be retained.
     *
     * @param values a collection of [Value]s
     * @return true iff all [values] should be retained
     */
    fun retain(values: Iterable<Value>) =
        values.fold(retain(values.elementAt(0))) { acc, value -> accumulate(acc, retain(value)) }
}

/**
 * Filters [Value]s based on their class.
 */
@Suppress("MethodOverloading") // That is how JimpleValueVisitor works
private class ClassValueFilterRule : ValueFilterRule() {
    override fun applyDefault(value: Value) = true

    override fun applyOther(value: Value) =
        throw UnsupportedValueException("Value of type ${value.javaClass} is not supported by the value filter.")

    override fun apply(value: NewArrayExpr) = false
    override fun apply(value: NewMultiArrayExpr) = false
    override fun apply(value: IdentityRef) = false
    override fun apply(value: EquivalentValue) = false
    override fun apply(value: NewStaticLock) = false
    override fun apply(value: AbstractDataSource) = false

    /**
     * Returns true iff [result1] and [result2] are both true.
     *
     * @param result1 a boolean
     * @param result2 a boolean
     * @return true iff [result1] and [result2] are both true
     */
    override fun accumulate(result1: Boolean, result2: Boolean) = result1 && result2
}

/**
 * Retains [Value]s iff they use a class from [libraryProject].
 *
 * @property libraryProject a library project
 */
@Suppress("MethodOverloading", "TooManyFunctions") // That is how JimpleValueVisitor works
private class LibraryUsageValueFilterRule(private val libraryProject: JavaProject) : ValueFilterRule() {
    override fun applyDefault(value: Value) = isLibraryClass(value.type)

    override fun apply(value: NewExpr) = isLibraryClass(value.baseType)
    override fun apply(value: NewArrayExpr) = isLibraryClass(value.baseType)
    override fun apply(value: NewMultiArrayExpr) = isLibraryClass(value.baseType)

    override fun apply(value: StaticInvokeExpr) = isLibraryClass(value.method.declaringClass)
    override fun apply(value: InstanceInvokeExpr) = isLibraryClass(value.method.declaringClass)
    override fun apply(value: DynamicInvokeExpr) = isLibraryClass(value.method.declaringClass)

    override fun apply(value: InstanceFieldRef) = isLibraryClass(value.field.declaringClass)
    override fun apply(value: StaticFieldRef) = isLibraryClass(value.field.declaringClass)

    override fun apply(value: Constant) = false

    override fun apply(value: InstanceOfExpr) = isLibraryClass(value.checkType)
    override fun apply(value: CastExpr) = isLibraryClass(value.castType)

    /**
     * Returns true if either [result1] or [result2] is true.
     *
     * @param result1 a boolean
     * @param result2 a boolean
     * @return true if either [result1] or [result2] is true
     */
    override fun accumulate(result1: Boolean, result2: Boolean) = result1 || result2

    private fun isLibraryClass(type: Type) = type !is PrimType && isLibraryClass(type.toString())
    private fun isLibraryClass(clazz: SootClass) = isLibraryClass(clazz.name)
    private fun isLibraryClass(className: String) = libraryProject.classNames.contains(className)
}

/**
 * Retains [Value]s that do not use user projects.
 *
 * @property libraryProject a library project
 */
@Suppress("MethodOverloading", "TooManyFunctions") // That is how JimpleValueVisitor works
private class UserUsageValueFilterRule(private val libraryProject: JavaProject) : ValueFilterRule() {
    override fun applyDefault(value: Value) = isNotUserClass(value.type)

    override fun apply(value: NewExpr) = isNotUserClass(value.baseType)
    override fun apply(value: NewArrayExpr) = isNotUserClass(value.baseType)
    override fun apply(value: NewMultiArrayExpr) = isNotUserClass(value.baseType)

    override fun apply(value: StaticInvokeExpr) = isNotUserClass(value.method.declaringClass)
    override fun apply(value: InstanceInvokeExpr) = isNotUserClass(value.method.declaringClass)
    override fun apply(value: DynamicInvokeExpr) = isNotUserClass(value.method.declaringClass)

    override fun apply(value: InstanceFieldRef) = isNotUserClass(value.field.declaringClass)
    override fun apply(value: StaticFieldRef) = isNotUserClass(value.field.declaringClass)

    override fun apply(value: Constant) = true

    override fun apply(value: InstanceOfExpr) = isNotUserClass(value.checkType)
    override fun apply(value: CastExpr) = isNotUserClass(value.castType)

    /**
     * Returns true iff [result1] and [result2] are true.
     *
     * @param result1 a boolean
     * @param result2 a boolean
     * @return true iff [result1] and [result2] are true
     */
    override fun accumulate(result1: Boolean, result2: Boolean) = result1 && result2

    private fun isNotUserClass(type: Type) = type is PrimType || isNotUserClass(type.toString())
    private fun isNotUserClass(clazz: SootClass) = isNotUserClass(clazz.name)
    private fun isNotUserClass(className: String) =
        libraryProject.classNames.contains(className)
            || Scene.v().forceResolve(className, SootClass.BODIES).isJavaLibraryClass
}

/**
 * Exception for encountered values that are not supported by the [ClassValueFilterRule].
 */
internal class UnsupportedValueException(message: String) : Exception(message)
