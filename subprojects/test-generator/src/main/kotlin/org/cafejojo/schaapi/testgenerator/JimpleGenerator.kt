package org.cafejojo.schaapi.testgenerator

import soot.Body
import soot.Local
import soot.SootClass
import soot.SootMethod
import soot.Type
import soot.Value
import soot.VoidType
import soot.jimple.Jimple
import soot.jimple.JimpleBody
import soot.jimple.Stmt
import soot.jimple.internal.ImmediateBox
import soot.jimple.internal.JReturnStmt
import soot.jimple.internal.VariableBox

/**
 * Jimple IR code generator.
 *
 * This IR code can then be converted to Java Bytecode.
 *
 * @property SootClass the class to generate tests for
 */
internal class JimpleGenerator(private val sootClass: SootClass) {
    /**
     * Generates a non-static soot method for the given [SootClass] with a body written in Jimple IR.
     *
     * Unbounded variables in the list of [Stmt]s are used as method parameters. All variables are stored as locals
     * of the method. If a return statement is found in the sequence, this statement is the last statement, even if it
     * is not the last statement in the [statements] sequence. If no return statement is found the method returns void.
     *
     * The method itself does no verification of the body of the method. Verification can be done by calling
     * [soot.Body.validate] on [soot.SootMethod.activeBody] to validate that the body is well formed.
     *
     * @param methodName the name the method should have
     * @param statements a list of [Stmt]s which should be converted into a method
     * @return [SootMethod] with a body of Jimple IR, and unbound variables as method parameters.
     */
    fun generateJimpleMethod(methodName: String, statements: List<Stmt>): SootMethod {
        val methodParams = findUnboundVariables(statements)
        val sootMethod = SootMethod(methodName, methodParams.map { it.type }, VoidType.v())
        sootClass.addMethod(sootMethod)

        val jimpleBody = Jimple.v().newBody(sootMethod)
        sootMethod.activeBody = jimpleBody

        addReferenceToThisToBody(jimpleBody)
        addParameterAssignmentsToBody(jimpleBody, methodParams)
        addStatementsToBody(jimpleBody, statements, methodParams)
        sootMethod.returnType = addReturnStatement(jimpleBody)

        return sootMethod
    }

    private fun addReferenceToThisToBody(jimpleBody: JimpleBody) {
        val thisRef = Jimple.v().newThisRef(sootClass.type)
        val thisLocalVar = Jimple.v().newLocal("this", thisRef.type)

        jimpleBody.locals.add(thisLocalVar)
        jimpleBody.units.add(Jimple.v().newIdentityStmt(thisLocalVar, thisRef))
    }

    private fun addParameterAssignmentsToBody(jimpleBody: Body, methodParams: Set<Value>) {
        methodParams.forEachIndexed { paramIndex, param ->
            if (param !is Local) throw ValueIsNotLocal(param)

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
                .map { it as? Local ?: throw ValueIsNotLocal(it) }
            )

            if (statement is JReturnStmt) return
        }
    }

    private fun addReturnStatement(jimpleBody: Body): Type {
        val lastStatement = jimpleBody.units.last()

        return when (lastStatement) {
            is JReturnStmt -> lastStatement.op.type
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
                .filter { it is VariableBox || it is ImmediateBox }
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
class ValueIsNotLocal(value: Value) : RuntimeException("$value cannot be stored as a local.")
