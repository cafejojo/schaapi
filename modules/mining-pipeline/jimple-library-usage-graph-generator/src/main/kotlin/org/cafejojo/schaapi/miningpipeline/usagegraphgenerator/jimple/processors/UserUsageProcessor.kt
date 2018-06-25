package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.processors

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters.UserUsageValueFilter
import org.cafejojo.schaapi.models.project.JavaProject
import soot.Body
import soot.BooleanType
import soot.IntType
import soot.jimple.IfStmt
import soot.jimple.IntConstant
import soot.jimple.Jimple
import soot.jimple.SwitchStmt

/**
 * Processes Jimple bodies such that they no longer contain usages of user projects.
 */
internal class UserUsageProcessor(project: JavaProject) : Processor {
    private val userUsageValueFilter = UserUsageValueFilter(project)

    /**
     * Removes usages of user projects from if-statements.
     *
     * @param body the body to process
     */
    override fun process(body: Body) =
        body.units
            .filter { it.branches() }
            .forEach { unit ->
                when {
                    unit is IfStmt && !userUsageValueFilter.retain(unit.condition) -> {
                        val userUsageLocal = Jimple.v().newLocal("userUsageLocal$localsInserted", BooleanType.v())
                        localsInserted++

                        unit.condition = Jimple.v().newEqExpr(userUsageLocal, IntConstant.v(1))
                    }
                    unit is SwitchStmt && !userUsageValueFilter.retain(unit.key) -> {
                        val userUsageLocal = Jimple.v().newLocal("userUsageLocal$localsInserted", IntType.v())
                        localsInserted++

                        unit.key = userUsageLocal
                    }
                }
            }

    private companion object {
        private var localsInserted = 0
    }
}
