package org.cafejojo.schaapi.testgenerator

import org.cafejojo.schaapi.usagegraphgenerator.SootNode
import soot.SootClass
import soot.SootMethod
import soot.ValueBox
import soot.VoidType
import soot.jimple.Jimple
import soot.jimple.internal.ImmediateBox
import soot.jimple.internal.VariableBox
import soot.shimple.Shimple

/**
 * Shimple IR generator.
 *
 * @property SootClass
 */
internal class ShimpleGenerator(private val c: SootClass) {
    /**
     * Generates a soot method with a body written in shimple code.
     *
     * Unbounded variables in the list of [SootNode]s are used as method parameters.
     *
     * @param methodName the name the method should have
     * @param statements a list of [SootNode]s which should be turned into a method
     * @return [SootMethod] with Shimple body, and unbound variables as method parameters
     */
    fun generateShimpleMethod(methodName: String, statements: List<SootNode>): SootMethod {
        val methodParams = generateMethodParams(statements)

        val method = SootMethod(methodName, methodParams.map { it.value.type }, VoidType.v())
        val body = Shimple.v().newBody(method)

        methodParams.forEachIndexed { paramIndex, valueBox ->
            val value = valueBox.value

            val argument = Jimple.v().newLocal(value.toString(), value.type)
            val identityReference = Jimple.v().newParameterRef(value.type, paramIndex)
            val identityStatement = Jimple.v().newIdentityStmt(argument, identityReference)

            body.locals.add(argument)
            body.units.add(identityStatement)
        }

        statements
            .map { it.unit }
            .forEach { unit ->
                body.units.add(unit)
                body.locals.addAll(unit.defBoxes.map { Jimple.v().newLocal(it.value.toString(), it.value.type) })
            }

        c.addMethod(method)
        method.activeBody = body

        return method
    }

    private fun generateMethodParams(statements: List<SootNode>): Set<ValueBox> {
        val methodParams = mutableSetOf<ValueBox>()
        val definitions = mutableSetOf<String>()

        statements.forEach { sootNode ->
            val unit = sootNode.unit

            unit.useAndDefBoxes
                .filter { it is VariableBox || it is ImmediateBox }
                .forEach { box ->
                    val identifier = box.value.toString()

                    when {
                        unit.defBoxes.contains(box) -> definitions.add(identifier)
                        !definitions.contains(identifier) -> methodParams.add(box)
                    }
                }
        }

        return methodParams
    }
}
