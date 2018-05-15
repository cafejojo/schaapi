package org.cafejojo.schaapi.testgenerator

import soot.Body
import soot.Local
import soot.Modifier
import soot.Scene
import soot.SootClass
import soot.SootMethod
import soot.Type
import soot.Value
import soot.VoidType
import soot.jimple.Jimple
import soot.jimple.Stmt
import soot.jimple.internal.ImmediateBox
import soot.jimple.internal.JReturnStmt
import soot.jimple.internal.JReturnVoidStmt
import soot.jimple.internal.JimpleLocalBox
import soot.jimple.internal.VariableBox

/**
 * Generates a [SootClass], and allows methods to be generated for said class based on lists of [Stmt]s.
 *
 * The body of methods is represented using [Jimple] IR. This IR can then be converted to java bytecode, or other IR
 * representations.
 *
 * @param className name of [SootClass] to be generated
 */
class SootClassGenerator(className: String) {
    init {
        Scene.v().loadClassAndSupport("java.lang.Object")
    }

    val sootClass = SootClass(className, Modifier.PUBLIC).apply {
        superclass = Scene.v().getSootClass("java.lang.Object")
    }

    /**
     * Generates a non-static soot method for the given [SootClass] with a body written in Jimple IR, and add it to the
     * class.
     *
     * Unbound variables in the list of [Stmt]s are used as method parameters. All variables are stored as locals
     * of the method. If a return statement is found in the sequence, this statement is the last statement, even if it
     * is not the last statement in the [statements] sequence. If no return statement is found the method returns void.
     *
     * The method itself does no verification of the body of the method. Verification can be done by calling
     * [soot.Body.validate] on [soot.SootMethod.activeBody] to validate that the body is well formed.
     *
     * The body of the method can after be converted to other IR representations or java bytecode.
     *
     * @param methodName the name the method should have
     * @param statements a list of [Stmt]s which should be converted into a method
     * @return [SootMethod] with a body of Jimple IR, and unbound variables as method parameters.
     */
    fun generateMethod(methodName: String, statements: List<Stmt>): SootMethod {
        val methodParams = findUnboundVariables(statements)
        val sootMethod = SootMethod(methodName, methodParams.map { it.type }, VoidType.v())
        sootMethod.modifiers = Modifier.PUBLIC.or(Modifier.STATIC)
        sootClass.addMethod(sootMethod)

        val jimpleBody = Jimple.v().newBody(sootMethod)
        sootMethod.activeBody = jimpleBody

        addParameterAssignmentsToBody(jimpleBody, methodParams)
        addStatementsToBody(jimpleBody, statements, methodParams)
        sootMethod.returnType = addReturnStatement(jimpleBody)

        return sootMethod
    }

    private fun addParameterAssignmentsToBody(jimpleBody: Body, methodParams: Set<Value>) {
        methodParams.forEachIndexed { paramIndex, param ->
            if (param !is Local) throw ValueIsNotLocalException(param)

            val identityReference = Jimple.v().newParameterRef(param.type, paramIndex)
            val identityStatement = Jimple.v().newIdentityStmt(param, identityReference)

            jimpleBody.locals.add(param)
            jimpleBody.units.add(identityStatement)
        }
    }

    private fun addStatementsToBody(jimpleBody: Body, statements: List<Stmt>, methodParams: Set<Value>) {
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

    private fun findUnboundVariables(statements: List<Stmt>): Set<Value> {
        val methodParams = mutableSetOf<Value>()
        val definitions = mutableSetOf<String>()

        statements.forEach { statement ->
            statement.useAndDefBoxes
                .filter { it is VariableBox || it is ImmediateBox || it is JimpleLocalBox }
                .forEach { box ->
                    val identifier = box.value.toString()
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
