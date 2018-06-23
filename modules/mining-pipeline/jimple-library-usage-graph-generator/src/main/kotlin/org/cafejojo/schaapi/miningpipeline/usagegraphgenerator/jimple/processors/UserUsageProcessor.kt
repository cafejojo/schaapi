package org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.processors

import org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.filters.UserUsageValueFilter
import org.cafejojo.schaapi.models.project.JavaProject
import soot.Body
import soot.BooleanType
import soot.jimple.IfStmt
import soot.jimple.IntConstant
import soot.jimple.Jimple

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
            .filterIsInstance<IfStmt>()
            .filterNot { userUsageValueFilter.retain(it.condition) }
            .forEach {
                val userUsageLocal = Jimple.v().newLocal("userUsageLocal$localsInserted", BooleanType.v())
                localsInserted++

                it.condition = Jimple.v().newEqExpr(userUsageLocal, IntConstant.v(1))
            }

    companion object {
        private var localsInserted = 0
    }
}
