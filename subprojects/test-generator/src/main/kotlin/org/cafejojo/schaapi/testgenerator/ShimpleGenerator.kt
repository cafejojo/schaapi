package org.cafejojo.schaapi.testgenerator

import org.cafejojo.schaapi.usagegraphgenerator.SootNode
import soot.Scene
import soot.SootClass
import soot.SootMethod
import soot.ValueBox
import soot.VoidType
import soot.jimple.internal.ImmediateBox
import soot.shimple.Shimple

/**
 * Shimple IR generator.
 *
 * @property statements a list of statements used to generate java bytecode. The generated java bytecode
 * should represent a method where the unbound variables are method parameters.
 */
internal class ShimpleGenerator(private val c: SootClass, private val statements: List<SootNode>) {
    /**
     * Generates a soot method with a body written in shimple code.
     *
     * Unbound variables in the list of [SootNode]s are used as method parameters.
     */
    fun generateShimple(): SootMethod {
        val methodParams = generateMethodParams()

        Scene.v().addClass(c)

        val method = SootMethod("method", methodParams.map { it.value.type }, VoidType.v())
        c.addMethod(method)

        val body = Shimple.v().newBody(method)
        // TODO add mapping from unbound variables to method parameters
        body.units.addAll(statements.map { it.unit })

        return method
    }

    private fun generateMethodParams(): Set<ValueBox> {
        val methodParams = mutableSetOf<ValueBox>()
        val definitions = mutableSetOf<String>()

        statements.forEach { sootNode ->
            val unit = sootNode.unit

            unit.useAndDefBoxes.forEach { valueBox ->
                // If not id, won't be in defBoxes
                val id = valueBox.value.toString()

                when {
                    unit.defBoxes.contains(valueBox) -> definitions.add(id)
                    !definitions.contains(id) && valueBox is ImmediateBox -> methodParams.add(valueBox)
                }
            }
        }

        return methodParams
    }
}
