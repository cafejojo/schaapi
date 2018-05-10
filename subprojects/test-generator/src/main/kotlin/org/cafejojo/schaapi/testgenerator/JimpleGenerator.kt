package org.cafejojo.schaapi.testgenerator

import soot.Local
import soot.SootClass
import soot.SootMethod
import soot.Value
import soot.VoidType
import soot.jimple.Jimple
import soot.jimple.Stmt
import soot.jimple.internal.ImmediateBox
import soot.jimple.internal.JReturnStmt
import soot.jimple.internal.VariableBox

/**
 * Jimple IR code generator.
 *
 * This IR code can then be converted to Java Bytecode.
 *
 * @property SootClass the class to generate tests for.
 */
internal class JimpleGenerator(private val sootClass: SootClass) {
    /**
     * Generates a soot method with a body written in Jimple IR.
     *
     * Unbounded variables in the list of [Stmt]s are used as method parameters. All variables are stored as locals
     * of the method. If the last statement is a return statement, then its value is the return value of the method.
     * Else the method returns nothing.
     *
     * The method itself does not verification of the body of the method, which can be accessed by using
     * [SootMethod.activeBody]. Verification can be done by calling [soot.Body.validate] to validate that the body is
     * well formed.
     *
     * @param methodName the name the method should have.
     * @param statements a list of [Stmt]s which should be converted into a method.
     * @return [SootMethod] with a body of Jimple IR, and unbound variables as method parameters.
     */
    fun generateJimpleMethod(methodName: String, statements: List<Stmt>): SootMethod {
        val methodParams = findUnboundVariables(statements)

        val method = SootMethod(methodName, methodParams.map { it.type }, VoidType.v())
        val jimpleBody = Jimple.v().newBody(method)

        methodParams.forEachIndexed { paramIndex, param ->
            val argument = param as Local
            val identityReference = Jimple.v().newParameterRef(param.type, paramIndex)
            val identityStatement = Jimple.v().newIdentityStmt(argument, identityReference)

            jimpleBody.locals.add(argument)
            jimpleBody.units.add(identityStatement)
        }

        statements.forEach { statement ->
            jimpleBody.units.add(statement)
            jimpleBody.locals.addAll(statement.defBoxes
                .map { it.value }
                .filter { !methodParams.contains(it) && it is Local }
                .map { it as Local }
            )
        }

        val lastStatement = statements.last()
        if (lastStatement is JReturnStmt) method.returnType = lastStatement.op.type
        else jimpleBody.units.add(Jimple.v().newReturnVoidStmt())

        sootClass.addMethod(method)
        method.activeBody = jimpleBody

        return method
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
