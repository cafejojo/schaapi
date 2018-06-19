package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import soot.EquivalentValue
import soot.Immediate
import soot.Local
import soot.Value
import soot.jimple.AnyNewExpr
import soot.jimple.ArrayRef
import soot.jimple.BinopExpr
import soot.jimple.CastExpr
import soot.jimple.Constant
import soot.jimple.DynamicInvokeExpr
import soot.jimple.Expr
import soot.jimple.FieldRef
import soot.jimple.IdentityRef
import soot.jimple.InstanceFieldRef
import soot.jimple.InstanceInvokeExpr
import soot.jimple.InstanceOfExpr
import soot.jimple.InvokeExpr
import soot.jimple.NewArrayExpr
import soot.jimple.NewExpr
import soot.jimple.NewMultiArrayExpr
import soot.jimple.Ref
import soot.jimple.StaticFieldRef
import soot.jimple.StaticInvokeExpr
import soot.jimple.UnopExpr
import soot.jimple.toolkits.infoflow.AbstractDataSource
import soot.jimple.toolkits.thread.synchronization.NewStaticLock

/**
 * Recursively visits the [Value]s contained within a [Value] and accumulates a result based on the implemented methods.
 */
@Suppress("MethodOverloading", "TooManyFunctions") // Result of the inverted implementation of Visitor
abstract class JimpleValueVisitor<R> {
    /**
     * This method is called at the start of [visit].
     *
     * @param value the [Value] that [visit] is called with
     */
    internal open fun beforeVisit(value: Value) {}

    /**
     * This method is called at the end of [visit].
     *
     * @param value the [Value] that [visit] is called with
     */
    internal open fun afterVisit(value: Value) {}

    /**
     * Recursively visits the [Value]s contained within [value], and applies the appropriate [apply] implementation to
     * the visited [Value]s. The results of multiple [apply] calls are combined using the [accumulate] method.
     *
     * @param value the [value] to recursively visit
     * @return the accumulated result of calling [apply] on all [Value]s recursively contained in [value]
     */
    fun visit(value: Value): R {
        beforeVisit(value)

        val result = when (value) {
            is Expr -> visitExpr(value)
            is Ref -> visitRef(value)
            is Immediate -> visitImmediate(value)
            is EquivalentValue -> accumulate(apply(value), visit(value.value))
            is NewStaticLock -> apply(value)
            is AbstractDataSource -> apply(value)
            else -> applyOther(value)
        }

        afterVisit(value)
        return result
    }

    /**
     * This method is called when a [Value] is visited by [visit] and all other [apply] methods are not applicable.
     *
     * @param value a [Value]
     */
    open fun applyOther(value: Value) = applyDefault(value)

    private fun visitExpr(value: Expr): R =
        when (value) {
            is UnopExpr -> accumulate(apply(value), visit(value.op))
            is BinopExpr -> accumulate(apply(value), visit(value.op1), visit(value.op2))
            is AnyNewExpr -> visitAnyNewExpr(value)
            is InvokeExpr -> visitInvokeExpr(value)
            is InstanceOfExpr -> accumulate(apply(value), visit(value.op))
            is CastExpr -> accumulate(apply(value), visit(value.op))
            else -> applyOtherExpr(value)
        }

    /**
     * This method is called when an [Expr] is visited by [visit] and all other [apply] methods are not applicable.
     *
     * @param value an [Expr]
     */
    open fun applyOtherExpr(value: Expr) = applyOther(value)

    private fun visitAnyNewExpr(value: AnyNewExpr) =
        when (value) {
            is NewExpr -> apply(value)
            is NewArrayExpr -> accumulate(apply(value), visit(value.size))
            is NewMultiArrayExpr -> accumulate(apply(value), value.sizes.map { visit(it) })
            else -> applyOtherAnyNewExpr(value)
        }

    /**
     * This method is called when an [AnyNewExpr] is visited by [visit] and all other [apply] methods are not
     * applicable.
     *
     * @param value an [AnyNewExpr]
     */
    open fun applyOtherAnyNewExpr(value: AnyNewExpr) = applyOtherExpr(value)

    private fun visitInvokeExpr(value: InvokeExpr) =
        when (value) {
            is InstanceInvokeExpr ->
                accumulate(
                    accumulate(apply(value), visit(value.base)),
                    value.args.map { visit(it) }
                )
            is StaticInvokeExpr -> accumulate(apply(value), value.args.map { visit(it) })
            is DynamicInvokeExpr ->
                accumulate(
                    accumulate(
                        apply(value),
                        value.bootstrapArgs.map { visit(it) }
                    ),
                    value.args.map { visit(it) }
                )
            else -> applyOtherInvokeExpr(value)
        }

    /**
     * This method is called when an [InvokeExpr] is visited by [visit] and all other [apply] methods are not
     * applicable.
     *
     * @param value an [InvokeExpr]
     */
    open fun applyOtherInvokeExpr(value: InvokeExpr) = applyOtherExpr(value)

    private fun visitRef(value: Ref) =
        when (value) {
            is IdentityRef -> apply(value)
            is FieldRef -> visitFieldRef(value)
            is ArrayRef -> accumulate(apply(value), visit(value.base), visit(value.index))
            else -> applyOtherRef(value)
        }

    /**
     * This method is called when a [Ref] is visited by [visit] and all other [apply] methods are not applicable.
     *
     * @param value a [Ref]
     */
    open fun applyOtherRef(value: Ref) = applyOther(value)

    private fun visitFieldRef(value: FieldRef) =
        when (value) {
            is InstanceFieldRef -> accumulate(apply(value), visit(value.base))
            is StaticFieldRef -> apply(value)
            else -> applyOtherFieldRef(value)
        }

    /**
     * This method is called when a [FieldRef] is visited by [visit] and all other [apply] methods are not applicable.
     *
     * @param value a [FieldRef]
     */
    open fun applyOtherFieldRef(value: FieldRef) = applyOtherRef(value)

    private fun visitImmediate(value: Immediate) =
        when (value) {
            is Local -> apply(value)
            is Constant -> apply(value)
            else -> applyOtherImmediate(value)
        }

    /**
     * This method is called when an [Immediate] is visited by [visit] and all other [apply] methods are not applicable.
     *
     * @param value an [Immediate]
     */
    open fun applyOtherImmediate(value: Immediate) = applyOther(value)

    /**
     * This method is called when a [UnopExpr] is visited by [visit].
     *
     * @param value a [UnopExpr]
     */
    open fun apply(value: UnopExpr) = applyDefault(value)

    /**
     * This method is called when a [BinopExpr] is visited by [visit].
     *
     * @param value a [BinopExpr]
     */
    open fun apply(value: BinopExpr) = applyDefault(value)

    /**
     * This method is called when a [NewExpr] is visited by [visit].
     *
     * @param value a [NewExpr]
     */
    open fun apply(value: NewExpr) = applyDefault(value)

    /**
     * This method is called when a [NewArrayExpr] is visited by [visit].
     *
     * @param value a [NewArrayExpr]
     */
    open fun apply(value: NewArrayExpr) = applyDefault(value)

    /**
     * This method is called when a [NewMultiArrayExpr] is visited by [visit].
     *
     * @param value a [NewMultiArrayExpr]
     */
    open fun apply(value: NewMultiArrayExpr) = applyDefault(value)

    /**
     * This method is called when a [StaticInvokeExpr] is visited by [visit].
     *
     * @param value a [StaticInvokeExpr]
     */
    open fun apply(value: StaticInvokeExpr) = applyDefault(value)

    /**
     * This method is called when an [InstanceInvokeExpr] is visited by [visit].
     *
     * @param value an [InstanceInvokeExpr]
     */
    open fun apply(value: InstanceInvokeExpr) = applyDefault(value)

    /**
     * This method is called when a [DynamicInvokeExpr] is visited by [visit].
     *
     * @param value a [DynamicInvokeExpr]
     */
    open fun apply(value: DynamicInvokeExpr) = applyDefault(value)

    /**
     * This method is called when an [IdentityRef] is visited by [visit].
     *
     * @param value an [IdentityRef]
     */
    open fun apply(value: IdentityRef) = applyDefault(value)

    /**
     * This method is called when an [InstanceFieldRef] is visited by [visit].
     *
     * @param value an [InstanceFieldRef]
     */
    open fun apply(value: InstanceFieldRef) = applyDefault(value)

    /**
     * This method is called when a [StaticFieldRef] is visited by [visit].
     *
     * @param value a [StaticFieldRef]
     */
    open fun apply(value: StaticFieldRef) = applyDefault(value)

    /**
     * This method is called when an [ArrayRef] is visited by [visit].
     *
     * @param value an [ArrayRef]
     */
    open fun apply(value: ArrayRef) = applyDefault(value)

    /**
     * This method is called when a [Local] is visited by [visit].
     *
     * @param value a [Local]
     */
    open fun apply(value: Local) = applyDefault(value)

    /**
     * This method is called when a [Constant] is visited by [visit].
     *
     * @param value a [Constant]
     */
    open fun apply(value: Constant) = applyDefault(value)

    /**
     * This method is called when an [InstanceOfExpr] is visited by [visit].
     *
     * @param value an [InstanceOfExpr]
     */
    open fun apply(value: InstanceOfExpr) = applyDefault(value)

    /**
     * This method is called when a [CastExpr] is visited by [visit].
     *
     * @param value a [CastExpr]
     */
    open fun apply(value: CastExpr) = applyDefault(value)

    /**
     * This method is called when an [EquivalentValue] is visited by [visit].
     *
     * @param value an [EquivalentValue]
     */
    open fun apply(value: EquivalentValue) = applyDefault(value)

    /**
     * This method is called when an [NewStaticLock] is visited by [visit].
     *
     * @param value an [NewStaticLock]
     */
    open fun apply(value: NewStaticLock) = applyDefault(value)

    /**
     * This method is called when an [AbstractDataSource] is visited by [visit].
     *
     * @param value an [AbstractDataSource]
     */
    open fun apply(value: AbstractDataSource) = applyDefault(value)

    /**
     * This method is called by default if an [apply] method is not overridden.
     *
     * @param value a [Value]
     */
    abstract fun applyDefault(value: Value): R

    /**
     * Combines two values of type [R].
     *
     * @param result1 a value
     * @param result2 a value
     * @return the combination of [result1] and [result2]
     */
    abstract fun accumulate(result1: R, result2: R): R

    private fun accumulate(result1: R, results: List<R>) = results.fold(result1) { acc, cur -> accumulate(acc, cur) }

    private fun accumulate(result1: R, vararg results: R) = accumulate(result1, results.toList())
}

/**
 * Lists all [Value]s that are recursively contained in a given value.
 */
class JimpleValueAccumulator : JimpleValueVisitor<List<Value>>() {
    /**
     * Returns a singleton list containing [value].
     *
     * @param value the [Value] to return in a singleton list
     * @return a singleton list containing [value]
     */
    override fun applyDefault(value: Value) = listOf(value)

    /**
     * Returns the union of the given lists.
     *
     * @param result1 a list
     * @param result2 a list
     * @return the union of the given lists
     */
    override fun accumulate(result1: List<Value>, result2: List<Value>) = result1.union(result2).toList()
}
