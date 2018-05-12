package org.cafejojo.schaapi.testgenerator

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
     * @param methodName the name the method should have.
     * @param statements a list of [Stmt]s which should be converted into a method.
     * @return [SootMethod] with a body of Jimple IR, and unbound variables as method parameters.
     */
    fun generateJimpleMethod(methodName: String, statements: List<Stmt>): SootMethod {
        val methodParams = findUnboundVariables(statements)

        val method = SootMethod(methodName, methodParams.map { it.type }, VoidType.v())
        val jimpleBody = Jimple.v().newBody(method)

        methodParams.forEachIndexed { paramIndex, param ->
            val argument = Jimple.v().newLocal(param.toString(), param.type)
            val identityReference = Jimple.v().newParameterRef(param.type, paramIndex)
            val identityStatement = Jimple.v().newIdentityStmt(argument, identityReference)

            jimpleBody.locals.add(argument)
            jimpleBody.units.add(identityStatement)
        }

        statements.forEach { statement ->
            jimpleBody.units.add(statement)
            jimpleBody.locals.addAll(statement.defBoxes
                .filter { !methodParams.contains(it.value) }
                .map { Jimple.v().newLocal(it.value.toString(), it.value.type) }
            )
        }

        val lastStatement = statements.last()
        if (lastStatement is JReturnStmt) method.returnType = lastStatement.op.type

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
