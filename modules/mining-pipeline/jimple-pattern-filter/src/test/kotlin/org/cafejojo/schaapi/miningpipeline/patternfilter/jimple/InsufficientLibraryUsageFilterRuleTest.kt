package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.cafejojo.schaapi.models.project.JavaProject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.RefType
import soot.Value
import soot.jimple.Jimple

/**
 * Unit tests for [InsufficientLibraryUsageFilterRule].
 */
internal object InsufficientLibraryUsageFilterRuleTest : Spek({
    fun createLocal(name: String, type: String) = Jimple.v().newLocal(name, RefType.v(type))

    fun createAssignStmt(op1: Value, op2: Value) = Jimple.v().newAssignStmt(op1, op2)

    fun createEqExpr(op1: Value, op2: Value) = Jimple.v().newEqExpr(op1, op2)

    describe("insufficient library usage filter") {
        lateinit var filter: InsufficientLibraryUsageFilterRule

        beforeEachTest {
            val project = mock<JavaProject> {
                on { it.classNames } doReturn setOf("ClassA", "ClassB", "ClassC")
            }
            filter = InsufficientLibraryUsageFilterRule(project, 2)
        }

        it("filters out empty patterns") {
            assertThat(filter.retain(listOf())).isFalse()
        }

        it("filters out patterns with too few usages") {
            val pattern = listOf(
                createAssignStmt(createLocal("localA", "ClassA"), createLocal("localB", "ClassB"))
            ).map { JimpleNode(it) }

            assertThat(filter.retain(pattern)).isFalse()
        }

        it("filters out patterns with enough statements but not enough usages") {
            val pattern = listOf(
                createAssignStmt(createLocal("localA", "ClassA"), createLocal("localB", "ClassB")),
                createAssignStmt(createLocal("localC", "InvalidA"), createLocal("localD", "InvalidB"))
            ).map { JimpleNode(it) }

            assertThat(filter.retain(pattern)).isFalse()
        }

        it("filters out patterns with many usages in only one statement") {
            val pattern = listOf(
                createAssignStmt(
                    createLocal("localA", "ClassA"),
                    createEqExpr(
                        createLocal("localB", "ClassB"),
                        createLocal("localD", "ClassC")
                    )
                ),
                createAssignStmt(createLocal("localE", "InvalidA"), createLocal("localF", "InvalidB"))
            ).map { JimpleNode(it) }

            assertThat(filter.retain(pattern)).isFalse()
        }

        it("retains patterns with exactly enough usages") {
            val pattern = listOf(
                createAssignStmt(createLocal("localA", "ClassA"), createLocal("localB", "ClassB")),
                createAssignStmt(createLocal("localC", "ClassC"), createLocal("localD", "ClassA"))
            ).map { JimpleNode(it) }

            assertThat(filter.retain(pattern)).isTrue()
        }
    }
})
