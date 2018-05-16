package org.cafejojo.schaapi.testgenerator

import org.cafejojo.schaapi.common.ClassGenerator
import org.cafejojo.schaapi.common.Node
import org.cafejojo.schaapi.usagegraphgenerator.SootNode
import soot.Body
import soot.Local
import soot.Modifier
import soot.Scene
import soot.SootClass
import soot.SootMethod
import soot.Type
import soot.Unit
import soot.Value
import soot.VoidType
import soot.jimple.Jimple
import soot.jimple.internal.JReturnStmt
import soot.jimple.internal.JReturnVoidStmt

/**
 * Generates a [SootClass], and allows methods to be generated for said class based on lists of [Unit]s.
 *
 * The body of methods is represented using [Jimple] IR. This IR can then be converted to java bytecode, or other IR
 * representations.
 *
 * @param className name of [SootClass] to be generated
 */
class SootClassGenerator(className: String) : ClassGenerator {
    init {
        Scene.v().addBasicClass("java.lang.Object")
    }

    val sootClass = SootClass(className, Modifier.PUBLIC).apply {
        superclass = Scene.v().getSootClass("java.lang.Object")
    }

    /**
     * Generates a non-static soot method for the given [SootClass] with a body written in Jimple IR, and add it to the
     * class.
     *
     * Unbound variables in the list of [Unit]s are used as method parameters. All variables are stored as locals
     * of the method. If a return statement is found in the sequence, this statement is the last statement, even if it
     * is not the last statement in the [statements] sequence. If no return statement is found the method returns void.
     *
     * The method itself does no verification of the body of the method. Verification can be done by calling
     * [soot.Body.validate] on [soot.SootMethod.activeBody] to validate that the body is well formed.
     *
     * The body of the method can after be converted to other IR representations or java bytecode.
     *
     * @param methodName the name the method should have
     * @param nodes a list of [Node]s which should be converted into a method
     */
    override fun generateMethod(methodName: String, nodes: List<Node>) {
        val statements = nodes.map {
            it as? SootNode ?: throw IllegalArgumentException("Cannot convert non-Soot nodes to methods.")
        }.map { it.unit }

        val methodParams = findUnboundVariables(statements)
        val sootMethod = SootMethod(methodName, methodParams.map { it.type }, VoidType.v())
        sootMethod.modifiers = Modifier.PUBLIC.or(Modifier.STATIC)
        sootClass.addMethod(sootMethod)

        val jimpleBody = Jimple.v().newBody(sootMethod)
        sootMethod.activeBody = jimpleBody

        addParameterAssignmentsToBody(jimpleBody, methodParams)
        addStatementsToBody(jimpleBody, statements, methodParams)
        sootMethod.returnType = addReturnStatement(jimpleBody)
    }

    /**
     * Writes the generated class to a class file.
     *
     * @param targetDirectory the path to the base directory in which to place the class file structure
     */
    override fun writeToFile(targetDirectory: String) = SootClassWriter.writeToFile(sootClass, targetDirectory)

    private fun addParameterAssignmentsToBody(jimpleBody: Body, methodParams: Set<Value>) {
        methodParams.forEachIndexed { paramIndex, param ->
            if (param !is Local) throw ValueIsNotLocalException(param)

            val identityReference = Jimple.v().newParameterRef(param.type, paramIndex)
            val identityStatement = Jimple.v().newIdentityStmt(param, identityReference)

            jimpleBody.locals.add(param)
            jimpleBody.units.add(identityStatement)
        }
    }

    private fun addStatementsToBody(jimpleBody: Body, statements: List<Unit>, methodParams: Set<Value>) {
        statements.forEach { statement ->
            jimpleBody.units.add(statement)
            jimpleBody.locals.addAll(statement.defBoxes
                .map { it.value }
                .filter { !methodParams.contains(it) }
                .map { it as? Local ?: throw ValueIsNotLocalException(it) }
            )

            if (statement is JReturnStmt) return
        }
    }

    private fun addReturnStatement(jimpleBody: Body): Type {
        val lastStatement = jimpleBody.units.last()

        return when (lastStatement) {
            is JReturnStmt -> lastStatement.op.type
            is JReturnVoidStmt -> VoidType.v()
            else -> {
                jimpleBody.units.add(Jimple.v().newReturnVoidStmt())
                VoidType.v()
            }
        }
    }

    private fun findUnboundVariables(statements: List<Unit>): Set<Value> {
        val methodParams = mutableSetOf<Value>()
        val definitions = mutableSetOf<String>()

        statements.forEach { statement ->
            statement.useAndDefBoxes
                .filter { it.value is Local }
                .forEach { box ->
                    val identifier =
                        (box.value as? Local)?.name ?: throw IllegalStateException("Value is no longer a local.")
                    when {
                        statement.defBoxes.contains(box) -> definitions.add(identifier)
                        !definitions.contains(identifier) -> methodParams.add(box.value)
                    }
                }
        }

        return methodParams
    }
}

/**
 * Exception to denote that a value cannot be stored as a local.
 */
class ValueIsNotLocalException(value: Value) : RuntimeException("$value cannot be stored as a local.")
