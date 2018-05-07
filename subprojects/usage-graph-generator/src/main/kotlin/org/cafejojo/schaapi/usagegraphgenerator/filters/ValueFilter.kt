package org.cafejojo.schaapi.usagegraphgenerator.filters

import soot.EquivalentValue
import soot.Immediate
import soot.Local
import soot.Value
import soot.jimple.AnyNewExpr
import soot.jimple.ArrayRef
import soot.jimple.BinopExpr
import soot.jimple.CastExpr
import soot.jimple.ConcreteRef
import soot.jimple.Constant
import soot.jimple.Expr
import soot.jimple.FieldRef
import soot.jimple.IdentityRef
import soot.jimple.InstanceOfExpr
import soot.jimple.InvokeExpr
import soot.jimple.NewArrayExpr
import soot.jimple.NewExpr
import soot.jimple.NewMultiArrayExpr
import soot.jimple.Ref
import soot.jimple.UnopExpr
import soot.jimple.internal.AbstractBinopExpr
import soot.jimple.toolkits.infoflow.AbstractDataSource
import soot.jimple.toolkits.thread.synchronization.NewStaticLock
import soot.shimple.PhiExpr
import soot.shimple.ShimpleExpr

/**
 * Performs filtering of library-using values.
 */
object ValueFilter {
    /**
     * Filters out non library-using values.
     *
     * @param value a value.
     * @return whether or not the value should be kept.
     */
    fun retain(value: Value): Boolean = when (value) {
        is Expr -> retainExpr(value)
        is Ref -> retainRef(value)
        is Immediate -> retainImmediate(value)
        is EquivalentValue -> retain(value.value)
        is NewStaticLock -> false // can never involve library usage
        is AbstractDataSource -> false // is only used for analysis purposes
        else -> throwUnrecognizedValue(value)
    }

    private fun retainExpr(expr: Expr) = when (expr) {
        is InvokeExpr -> isLibraryClass(expr.method.declaringClass.name)
        is UnopExpr -> retain(expr.op)
        is BinopExpr -> retain(expr.op1) || retain(expr.op2)
        is AbstractBinopExpr -> retain(expr.op1) || retain(expr.op2)
        is InstanceOfExpr -> retain(expr.op) && isLibraryClass(expr.checkType.toString())
        is ShimpleExpr -> retainShimpleExpr(expr)
        is AnyNewExpr -> retainAnyNewExpr(expr)
        is CastExpr -> retain(expr.op)
        else -> throwUnrecognizedValue(expr)
    }

    private fun retainShimpleExpr(expr: ShimpleExpr) = when (expr) {
        is PhiExpr -> expr.values.filter { retain(it) }.any()
        else -> throwUnrecognizedValue(expr)
    }

    private fun retainAnyNewExpr(expr: AnyNewExpr) = when (expr) {
        is NewExpr -> isLibraryClass(expr.type.toString())
        is NewArrayExpr -> false
        is NewMultiArrayExpr -> false
        else -> throwUnrecognizedValue(expr)
    }

    private fun retainRef(ref: Ref) = when (ref) {
        is IdentityRef -> false
        is ConcreteRef -> when (ref) {
            is FieldRef -> isLibraryClass(ref.field.declaringClass.name)
            is ArrayRef -> retain(ref.base)
            else -> throwUnrecognizedValue(ref)
        }
        else -> throwUnrecognizedValue(ref)
    }

    private fun retainImmediate(immediate: Immediate) = when (immediate) {
        is Local -> false
        is Constant -> false
        else -> throwUnrecognizedValue(immediate)
    }

    private fun isLibraryClass(className: String) =
        className.startsWith("org.cafejojo.schaapi.usagegraphgenerator.testclasses.library") // todo

    private fun throwUnrecognizedValue(value: Value): Nothing =
        throw UnsupportedValueException("Value of type ${value.javaClass} is not supported by the value filter.")
}

/**
 * Exception for encountered values that are not supported by the [ValueFilter].
 */
class UnsupportedValueException(message: String) : Exception(message)
