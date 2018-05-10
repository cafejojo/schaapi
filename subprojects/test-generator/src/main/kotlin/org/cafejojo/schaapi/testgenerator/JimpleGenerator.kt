package org.cafejojo.schaapi.testgenerator

import soot.Body
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
     * Generates a non-static soot method for the given [SootClass] with a body written in Jimple IR.
     *
     * Unbounded variables in the list of [Stmt]s are used as method parameters. All variables are stored as locals
     * of the method. If the last statement is a return statement, then its value is the return value of the method.
     * Else the method returns nothing.
     *
     * The method itself does not verification of the body of the method, which can be accessed by using
     * [SootMethod.activeBody]. Verification can be done by calling [soot.Body.validate] to validate that the body is
     * well formed.
     *
     * @addParamToLocals methodName the name the method should have.
     * @addParamToLocals statements a list of [Stmt]s which should be converted into a method.
     * @return [SootMethod] with a body of Jimple IR, and unbound variables as method parameters.
     */
    fun generateJimpleMethod(methodName: String, statements: List<Stmt>): SootMethod {
        val methodParams = findUnboundVariables(statements)

        val method = SootMethod(methodName, methodParams.map { it.type }, VoidType.v())
        val jimpleBody = Jimple.v().newBody(method)
        method.activeBody = jimpleBody
        sootClass.addMethod(method)

        val thisRef = Jimple.v().newThisRef(sootClass.type)
        val thisLocalVar = Jimple.v().newLocal("this", thisRef.type)
        jimpleBody.locals.add(thisLocalVar)
        jimpleBody.units.add(Jimple.v().newIdentityStmt(thisLocalVar, thisRef))

        methodParams.forEachIndexed { paramIndex, param -> addParamToLocals(jimpleBody, paramIndex, param) }
        statements.forEach { statement ->
            jimpleBody.units.add(statement)
            jimpleBody.locals.addAll(statement.defBoxes
                .map { it.value }
                .filter { !methodParams.contains(it) }
                .map { it as? Local ?: throw BoxValueIsNotLocal(it) }
            )
        }

        val lastStatement = statements.last()
        when (lastStatement) {
            is JReturnStmt -> method.returnType = lastStatement.op.type
            else -> jimpleBody.units.add(Jimple.v().newReturnVoidStmt())
        }

        return method
    }

    private fun addParamToLocals(jimpleBody: Body, paramIndex: Int, param: Value) {
        if (param !is Local) throw BoxValueIsNotLocal(param)

        val identityReference = Jimple.v().newParameterRef(param.type, paramIndex)
        val identityStatement = Jimple.v().newIdentityStmt(param, identityReference)

        jimpleBody.locals.add(param)
        jimpleBody.units.add(identityStatement)
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
class BoxValueIsNotLocal(value: Value) : RuntimeException("$value cannot be stored as a local.")
