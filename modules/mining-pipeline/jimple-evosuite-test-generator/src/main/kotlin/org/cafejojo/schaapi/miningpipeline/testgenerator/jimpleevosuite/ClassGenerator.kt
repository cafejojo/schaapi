package org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite

import org.cafejojo.schaapi.models.Node
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
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
import soot.jimple.ArrayRef
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.InstanceFieldRef
import soot.jimple.Jimple
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
     * Generates a non-static soot method for the given [SootClass] with a body written in Jimple IR, and add it to the
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
            jimpleBody.locals.addAll(statement.defBoxes
                .map { it.value }
                .filterNot { methodParams.contains(it) || it is InstanceFieldRef || it is ArrayRef }
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
 * Exception to denote that a value cannot be stored as a local.
 */
internal class ValueIsNotLocalException(value: Value) : RuntimeException("$value cannot be stored as a local.")

fun List<JimpleNode>.duplicate(): List<JimpleNode> {
    val oldToNewNodes = mutableMapOf<Stmt, Stmt>()
    val newNodes = this.map { oldNode ->
        oldNode.copy().also { oldToNewNodes[oldNode.statement] = it.statement }
    }

    newNodes.forEach {
        when (it.statement) {
            is GotoStmt -> {
                val statement = it.statement as GotoStmt
                statement.target = oldToNewNodes[statement]
            }
            is IfStmt -> {
                val statement = it.statement as IfStmt
                statement.setTarget(oldToNewNodes[statement])
            }
            is SwitchStmt -> {
                val statement = it.statement as SwitchStmt
                statement.defaultTarget = oldToNewNodes[statement]
                statement.targets.forEachIndexed { index, target ->
                    statement.setTarget(index, oldToNewNodes[target])
                }
            }
        }
    }

    return newNodes
}
