package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.libraryusagefilters

import mu.KLogging
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleValueVisitor
import org.cafejojo.schaapi.models.project.JavaProject
import soot.AnySubType
import soot.ArrayType
import soot.EquivalentValue
import soot.PrimType
import soot.RefType
import soot.SootClass
import soot.Type
import soot.Value
import soot.jimple.CastExpr
import soot.jimple.ClassConstant
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
 *
 * @property filterRules the rules to apply to the values
 */
open class ValueFilter(private val filterRules: List<ValueFilterRule>) {
    constructor(libraryProject: JavaProject) : this(
        listOf(
            ClassValueFilterRule(),
            LibraryUsageValueFilterRule(libraryProject),
            UserUsageValueFilterRule(libraryProject)
        )
    )

    /**
     * Returns true iff all [values] are of a useful class (as determined by [ClassValueFilterRule]), at least one of
     * them has a library usage, and none of them has a user project usage.
     *
     * If [values] is empty, true is returned.
     *
     * @param values a collection of [Value]s
     * @return true iff all [values] are of a useful class (as determined by [ClassValueFilterRule]), at least one of
     * them has a library usage, and none of them has a user project usage
     */
    fun retain(vararg values: Value) = filterRules.all { it.retain(values.toList()) }
}

/**
 * Filters [Value]s based only on their usage of classes from user projects.
 */
class UserUsageValueFilter(libraryProject: JavaProject) : ValueFilter(listOf(UserUsageValueFilterRule(libraryProject)))

/**
 * Describes how a [Value] should be filtered.
 */
abstract class ValueFilterRule : JimpleValueVisitor<Boolean>() {
    companion object : KLogging()

    /**
     * Returns true iff [value] should be retained.
     *
     * @param value a [Value]
     * @return true iff [value] should be retained
     */
    fun retain(value: Value) = visit(value)

    /**
     * Returns true iff all [values] should be retained or [values] is empty.
     *
     * @param values a collection of [Value]s
     * @return true iff all [values] should be retained or [values] is empty
     */
    fun retain(values: List<Value>) =
        values.isEmpty() || values.fold(retain(values.first())) { acc, value -> accumulate(acc, retain(value)) }
}

/**
 * Filters [Value]s based on their class.
 */
@Suppress("MethodOverloading") // That is how JimpleValueVisitor works
private class ClassValueFilterRule : ValueFilterRule() {
    override fun applyDefault(value: Value) = true

    override fun applyOther(value: Value) =
        throw UnsupportedValueException("Value of type ${value.javaClass} is not supported by the value filter.")

    override fun apply(value: Constant) = value !is ClassConstant || !isLambdaType(value.value)
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

    /**
     * Returns true iff [type] is a lambda type signature.
     *
     * @param type a JNI type descriptor
     */
    private fun isLambdaType(type: String) = type.startsWith('(')
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

    override fun apply(value: Constant) = value is ClassConstant && isLibraryClass(value.toSootType())

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

    @Suppress("TooGenericExceptionCaught") // We want to catch RuntimeExceptions as Soot only throws RuntimeExceptions
    override fun apply(value: Constant) = try {
        value !is ClassConstant || isNotUserClass(value.toSootType())
    } catch (e: RuntimeException) {
        logger.warn { e.message }
        false
    }

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

    private fun isNotUserClass(type: Type): Boolean =
        when (type) {
            is ArrayType -> isNotUserClass(type.baseType)
            is AnySubType -> isNotUserClass(type.base)
            is RefType -> isNotUserClass(type.sootClass)
            else -> true
        }

    private fun isNotUserClass(clazz: SootClass) =
        libraryProject.classNames.contains(clazz.name) || clazz.isJavaLibraryClass
}

/**
 * Exception for encountered values that are not supported by the [ClassValueFilterRule].
 */
internal class UnsupportedValueException(message: String) : Exception(message)
