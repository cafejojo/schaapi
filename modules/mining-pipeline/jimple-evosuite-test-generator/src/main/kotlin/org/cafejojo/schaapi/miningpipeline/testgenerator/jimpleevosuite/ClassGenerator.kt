package org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite

import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import soot.Body
import soot.BooleanType
import soot.ByteType
import soot.CharType
import soot.DoubleType
import soot.FloatType
import soot.IntType
import soot.Local
import soot.LongType
import soot.Modifier
import soot.Scene
import soot.ShortType
import soot.SootClass
import soot.SootMethod
import soot.Type
import soot.Unit
import soot.Value
import soot.VoidType
import soot.jimple.ArrayRef
import soot.jimple.DoubleConstant
import soot.jimple.FieldRef
import soot.jimple.FloatConstant
import soot.jimple.GotoStmt
import soot.jimple.IdentityStmt
import soot.jimple.IfStmt
import soot.jimple.IntConstant
import soot.jimple.Jimple
import soot.jimple.LongConstant
import soot.jimple.NullConstant
import soot.jimple.Stmt
import soot.jimple.SwitchStmt
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
internal class ClassGenerator(className: String) {
    init {
        Scene.v().addBasicClass("java.lang.Object")
    }

    val sootClass = SootClass(className, Modifier.PUBLIC).apply {
        superclass = Scene.v().getSootClass("java.lang.Object")
    }

    /**
     * Generates a non-static soot method for the given [SootClass] with a body written in Jimple IR, and adds it to the
     * class.
     *
     * Unbound variables in the list of [Unit]s are used as method parameters. All variables are stored as locals
     * of the method. If a return statement is found in the sequence, this statement is the last statement, even if it
     * is not the last statement in the statements sequence. If no return statement is found the method returns void.
     *
     * The method itself does no verification of the body of the method. Verification can be done by calling
     * [soot.Body.validate] on [soot.SootMethod.activeBody] to validate that the body is well formed.
     *
     * The body of the method can after be converted to other IR representations or java bytecode.
     *
     * @param methodName the name the method should have
     * @param nodes a list of [Node]s which should be converted into a method
     */
    fun generateMethod(methodName: String, nodes: List<JimpleNode>) {
        val statements = nodes.duplicate().map { it.statement }
        val methodParams = findUnboundVariables(statements)
        val sootMethod = SootMethod(methodName, methodParams.map { it.type }, VoidType.v())
        sootMethod.modifiers = Modifier.PUBLIC.or(Modifier.STATIC)
        sootClass.addMethod(sootMethod)

        val jimpleBody = Jimple.v().newBody(sootMethod)
        sootMethod.activeBody = jimpleBody

        addParameterAssignmentsToBody(jimpleBody, methodParams)
        addStatementsToBody(jimpleBody, statements, methodParams)
        sootMethod.returnType = addReturnStatement(jimpleBody)

        replaceInvalidTargets(statements)
    }

    /**
     * Writes the generated class to a class file.
     *
     * @param targetDirectory the path to the base directory in which to place the class file structure
     */
    fun writeToFile(targetDirectory: String) = ClassWriter.writeToFile(sootClass, targetDirectory)

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

            val unboundLocals = statement.defBoxes
                .map { it.value }
                .filterNot {
                    methodParams.contains(it) || jimpleBody.locals.contains(it)
                        || it is FieldRef || it is ArrayRef
                }
                .map { it as? Local ?: throw ValueIsNotLocalException(it) }

            jimpleBody.locals.addAll(unboundLocals)
            unboundLocals.forEach { addLocalInitializationsAfterIdentityStatements(jimpleBody, it) }

            if (statement is JReturnStmt) return
        }
    }

    private fun addLocalInitializationsAfterIdentityStatements(jimpleBody: Body, local: Local) {
        val defaultValue = getDefaultValueForType(local.type)
        val assignStatement = Jimple.v().newAssignStmt(local, defaultValue)

        if (jimpleBody.units.first is IdentityStmt)
            jimpleBody.units.insertAfter(assignStatement, jimpleBody.units.last { it is IdentityStmt })
        else
            jimpleBody.units.addFirst(assignStatement)
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
            statement.useBoxes
                .map { it.value }
                .filterIsInstance(Local::class.java)
                .filter { !definitions.contains(it.name) }
                .forEach {
                    methodParams.add(it)
                    definitions.add(it.name)
                }

            statement.defBoxes
                .map { it.value }
                .filterIsInstance(Local::class.java)
                .forEach { definitions.add(it.name) }
        }

        return methodParams
    }

    /**
     * Replaces invalid targets in [statements] with targets to the last statement.
     *
     * @param statements the statements to replace invalid targets in
     */
    private fun replaceInvalidTargets(statements: List<Unit>) {
        val targetReplacement = statements.last()

        statements.forEach { statement ->
            when {
                statement is GotoStmt && !statements.contains(statement.target) -> statement.target = targetReplacement
                statement is IfStmt && !statements.contains(statement.target) -> statement.setTarget(targetReplacement)
                statement is SwitchStmt -> {
                    if (!statements.contains(statement.defaultTarget)) statement.defaultTarget = targetReplacement

                    statement.targets.forEachIndexed { index, target ->
                        if (!statements.contains(target)) statement.setTarget(index, targetReplacement)
                    }
                }
            }
        }
    }
}

/**
 * Duplicates the list of [JimpleNode]s, restoring references between [Stmt]s.
 */
private fun List<JimpleNode>.duplicate(): List<JimpleNode> {
    val newNodes = this.map { it.copy() }
    val oldToNewStatements = newNodes
        .mapIndexed { index, newNode -> get(index).statement to newNode.statement }
        .toMap()

    newNodes.forEach {
        val statement = it.statement
        when (statement) {
            is GotoStmt -> statement.target = oldToNewStatements[statement.target]
            is IfStmt -> statement.setTarget(oldToNewStatements[statement.target])
            is SwitchStmt -> {
                statement.defaultTarget = oldToNewStatements[statement.defaultTarget]
                statement.targets.forEachIndexed { index, target ->
                    statement.setTarget(index, oldToNewStatements[target])
                }
            }
        }
    }

    return newNodes
}

private fun getDefaultValueForType(type: Type) = when (type) {
    is BooleanType -> IntConstant.v(0)
    is ByteType -> IntConstant.v(0)
    is CharType -> IntConstant.v(0)
    is DoubleType -> DoubleConstant.v(0.0)
    is FloatType -> FloatConstant.v(0f)
    is IntType -> IntConstant.v(0)
    is LongType -> LongConstant.v(0)
    is ShortType -> IntConstant.v(0)
    else -> NullConstant.v()
}

/**
 * Exception to denote that a value cannot be stored as a local.
 */
internal class ValueIsNotLocalException(value: Value) : RuntimeException("$value cannot be stored as a local.")
