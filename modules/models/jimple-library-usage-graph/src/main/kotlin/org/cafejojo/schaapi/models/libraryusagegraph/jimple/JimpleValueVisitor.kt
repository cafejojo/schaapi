package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import soot.Immediate
import soot.Value
import soot.jimple.AnyNewExpr
import soot.jimple.BinopExpr
import soot.jimple.CastExpr
import soot.jimple.DynamicInvokeExpr
import soot.jimple.InstanceInvokeExpr
import soot.jimple.InstanceOfExpr
import soot.jimple.Ref
import soot.jimple.StaticInvokeExpr
import soot.jimple.UnopExpr

/**
 * Recursively visits the [Value]s contained within a [Value] and accumulates a result based on the implemented methods.
 */
@SuppressWarnings("MethodOverloading", "TooManyFunctions") // Result of the inverted implementation of Visitor
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
            is UnopExpr -> accumulate(apply(value), visit(value.op))
            is BinopExpr -> accumulate(apply(value), visit(value.op1), visit(value.op2))
            is AnyNewExpr -> apply(value)
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
            is Ref -> apply(value)
            is Immediate -> apply(value)
            is InstanceOfExpr -> accumulate(apply(value), visit(value.op))
            is CastExpr -> accumulate(apply(value), visit(value.op))
            else -> applyOther(value)
        }

        afterVisit(value)
        return result
    }

    /**
     * This method is called when a [UnopExpr] is visited by [visit].
     *
     * @param value a [UnopExpr]
     */
    open fun apply(value: UnopExpr) = defaultApply(value)

    /**
     * This method is called when a [BinopExpr] is visited by [visit].
     *
     * @param value a [BinopExpr]
     */
    open fun apply(value: BinopExpr) = defaultApply(value)

    /**
     * This method is called when an [AnyNewExpr] is visited by [visit].
     *
     * @param value an [AnyNewExpr]
     */
    open fun apply(value: AnyNewExpr) = defaultApply(value)

    /**
     * This method is called when a [StaticInvokeExpr] is visited by [visit].
     *
     * @param value a [StaticInvokeExpr]
     */
    open fun apply(value: StaticInvokeExpr) = defaultApply(value)

    /**
     * This method is called when an [InstanceInvokeExpr] is visited by [visit].
     *
     * @param value an [InstanceInvokeExpr]
     */
    open fun apply(value: InstanceInvokeExpr) = defaultApply(value)

    /**
     * This method is called when a [DynamicInvokeExpr] is visited by [visit].
     *
     * @param value a [DynamicInvokeExpr]
     */
    open fun apply(value: DynamicInvokeExpr) = defaultApply(value)

    /**
     * This method is called when a [Ref] is visited by [visit].
     *
     * @param value a [Ref]
     */
    open fun apply(value: Ref) = defaultApply(value)

    /**
     * This method is called when an [Immediate] is visited by [visit].
     *
     * @param value an [Immediate]
     */
    open fun apply(value: Immediate) = defaultApply(value)

    /**
     * This method is called when an [InstanceOfExpr] is visited by [visit].
     *
     * @param value an [InstanceOfExpr]
     */
    open fun apply(value: InstanceOfExpr) = defaultApply(value)

    /**
     * This method is called when a [CastExpr] is visited by [visit].
     *
     * @param value a [CastExpr]
     */
    open fun apply(value: CastExpr) = defaultApply(value)

    /**
     * This method is called when a [Value] is visited by [visit] and the other [apply] methods are not applicable.
     *
     * @param value a [Value]
     */
    open fun applyOther(value: Value) = defaultApply(value)

    /**
     * This method is called by default is an [apply] method is not implemented.
     *
     * @param value a [Value]
     */
    abstract fun defaultApply(value: Value): R

    /**
     * Combines two values of type [R].
     *
     * @param result1 a value
     * @param result2 a value
     * @return the combination of [result1] and [result2]
     */
    abstract fun accumulate(result1: R, result2: R): R

    private fun accumulate(result1: R, results: List<R>) = results.fold(result1, { acc, cur -> accumulate(acc, cur) })

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
    override fun defaultApply(value: Value) = listOf(value)

    /**
     * Returns the union of the given lists.
     *
     * @param result1 a list
     * @param result2 a list
     * @return the union of the given lists
     */
    override fun accumulate(result1: List<Value>, result2: List<Value>) = result1.union(result2).toList()
}
