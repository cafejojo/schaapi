package org.cafejojo.schaapi.miningpipeline.testgenerator.jimpleevosuite

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.cafejojo.schaapi.models.libraryusagegraph.jimple.JimpleNode
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.AbstractSootFieldRef
import soot.BooleanType
import soot.CharType
import soot.DoubleType
import soot.FloatType
import soot.IntType
import soot.LongType
import soot.Modifier
import soot.NullType
import soot.RefType
import soot.Scene
import soot.SootClass
import soot.SootField
import soot.VoidType
import soot.jimple.AssignStmt
import soot.jimple.DoubleConstant
import soot.jimple.FloatConstant
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.IntConstant
import soot.jimple.Jimple
import soot.jimple.LongConstant
import soot.jimple.NullConstant
import soot.jimple.Stmt
import soot.jimple.StringConstant
import soot.jimple.SwitchStmt
import soot.jimple.internal.JEqExpr
import soot.options.Options
import java.io.File

@Suppress("UnsafeCast") // Casting statement targets to [Stmt] instances
internal object ClassGeneratorTest : Spek({
    beforeGroup {
        Options.v().set_soot_classpath(
            arrayOf(
                System.getProperty("java.home") + "${File.separator}lib${File.separator}rt.jar",
                System.getProperty("java.home") + "${File.separator}lib${File.separator}jce.jar"
            ).joinToString(File.pathSeparator)
        )

        Scene.v().loadNecessaryClasses()
    }

    describe("generation of a method based on a list of nodes") {
        it("should not create parameters if all variables are bound") {
            val a = Jimple.v().newLocal("a", CharType.v())
            val b = Jimple.v().newLocal("b", CharType.v())
            val c = Jimple.v().newLocal("c", CharType.v())

            val assignA = Jimple.v().newAssignStmt(a, StringConstant.v("hello"))
            val assignB = Jimple.v().newAssignStmt(b, StringConstant.v("world"))
            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val jimpleMethod = ClassGenerator("asdf").apply {
                generateMethod("method", listOf(assignA, assignB, assignC).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(jimpleMethod.parameterCount).isZero()
        }

        it("should not create parameters if all variables are bound") {
            val a = Jimple.v().newLocal("a", CharType.v())
            val b = Jimple.v().newLocal("b", CharType.v())
            val c = Jimple.v().newLocal("c", CharType.v())

            val assignA = Jimple.v().newAssignStmt(a, StringConstant.v("hello"))
            val assignB = Jimple.v().newAssignStmt(b, StringConstant.v("world"))
            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val jimpleMethod = ClassGenerator("asdf").apply {
                generateMethod("method", listOf(assignA, assignB, assignC).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(jimpleMethod.parameterCount).isZero()
        }

        it("should generate parameters for all unbound variables") {
            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", IntType.v())
            val c = Jimple.v().newLocal("c", IntType.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

            val jimpleMethod = ClassGenerator("class").apply {
                generateMethod("method", listOf(assignC).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(jimpleMethod.parameterTypes).containsExactly(IntType.v(), IntType.v())
            assertThat(jimpleMethod.activeBody.parameterLocals.map { it.name }).containsExactly(a.name, b.name)
        }

        it("should ignore unbound array references on the left-hand side of an assignment") {
            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", IntType.v())
            val c = Jimple.v().newArrayRef(b, IntConstant.v(0))

            val assignC = Jimple.v().newAssignStmt(c, a)

            val jimpleMethod = ClassGenerator("class").apply {
                generateMethod("method", listOf(assignC).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(jimpleMethod.parameterTypes).containsExactly(IntType.v(), IntType.v())
            assertThat(jimpleMethod.activeBody.parameterLocals.map { it.name }).containsExactly(b.name, a.name)
        }

        it("should ignore unbound instance field references") {
            val classWithAField = SootClass("ClassWithAField", Modifier.PUBLIC)
                .apply { addField(SootField("field", IntType.v(), Modifier.PUBLIC)) }

            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", classWithAField.type)
            val c = Jimple.v().newInstanceFieldRef(
                b, AbstractSootFieldRef(classWithAField, "field", IntType.v(), false)
            )

            val assignC = Jimple.v().newAssignStmt(c, a)

            val jimpleMethod = ClassGenerator("class").apply {
                generateMethod("method", listOf(assignC).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(jimpleMethod.parameterTypes).containsExactly(classWithAField.type, IntType.v())
            assertThat(jimpleMethod.activeBody.parameterLocals.map { it.name }).containsExactly(b.name, a.name)
        }

        it("should ignore unbound static field references") {
            val classWithAField = SootClass("ClassWithAField", Modifier.PUBLIC)
                .apply { addField(SootField("field", IntType.v(), Modifier.STATIC + Modifier.PUBLIC)) }

            val a = Jimple.v().newLocal("a", IntType.v())
            val c = Jimple.v().newStaticFieldRef(AbstractSootFieldRef(classWithAField, "field", IntType.v(), true))

            val assignC = Jimple.v().newAssignStmt(c, a)

            val jimpleMethod = ClassGenerator("class").apply {
                generateMethod("method", listOf(assignC).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(jimpleMethod.parameterTypes).containsExactly(IntType.v())
            assertThat(jimpleMethod.activeBody.parameterLocals.map { it.name }).containsExactly(a.name)
        }

        it("generates a class with the correct name") {
            val generator = ClassGenerator("ghjk")
            assertThat(generator.sootClass.name).isEqualTo("ghjk")
        }

        it("can generate multiple methods") {
            val a = Jimple.v().newLocal("a", IntType.v())
            val b = Jimple.v().newLocal("b", IntType.v())
            val c = Jimple.v().newLocal("c", IntType.v())
            val d = Jimple.v().newLocal("d", IntType.v())

            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))
            val assignD = Jimple.v().newAssignStmt(d, IntConstant.v(23))

            val generator = ClassGenerator("ghjk")

            val method1 = generator.apply {
                generateMethod("method1", listOf(assignC).map { JimpleNode(it) })
            }.sootClass.methods.last()
            val method2 = generator.apply {
                generateMethod("method2", listOf(assignD).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(method1.parameterCount).isEqualTo(2)
            assertThat(method2.parameterCount).isZero()
            assertThat(generator.sootClass.methodCount).isEqualTo(2)
        }

        it("adds a parameter for a value that is initialised recursively") {
            val local = Jimple.v().newLocal("a", IntType.v())
            val assign = Jimple.v().newAssignStmt(local, local)

            val method = ClassGenerator("Test").also {
                it.generateMethod("method", listOf(assign).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(method.parameterCount).isEqualTo(1)
        }

        it("does not add a parameter for a value that is assigned recursively after initialization") {
            val local = Jimple.v().newLocal("a", IntType.v())
            val assignA = Jimple.v().newAssignStmt(local, IntConstant.v(58))
            val assignB = Jimple.v().newAssignStmt(local, local)

            val method = ClassGenerator("Test").also {
                it.generateMethod("method", listOf(assignA, assignB).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(method.parameterCount).isEqualTo(0)
        }
    }

    it("should generate a valid body") {
        val a = Jimple.v().newLocal("a", IntType.v())
        val b = Jimple.v().newLocal("b", IntType.v())
        val c = Jimple.v().newLocal("c", IntType.v())

        val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))

        val jimpleMethod = ClassGenerator("myClass").apply {
            generateMethod("method", listOf(assignC).map { JimpleNode(it) })
        }.sootClass.methods.last()

        val throwable = catchThrowable { jimpleMethod.activeBody.validate() }
        assertThat(throwable).isNull()
    }

    it("should generate parameters for variables only bound after their use") {
        val a = Jimple.v().newLocal("a", IntType.v())
        val b = Jimple.v().newLocal("b", IntType.v())
        val c = Jimple.v().newLocal("c", IntType.v())

        val assignA = Jimple.v().newAssignStmt(a, IntConstant.v(10))
        val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))
        val assignB = Jimple.v().newAssignStmt(b, IntConstant.v(20))

        val jimpleMethod = ClassGenerator("classy").apply {
            generateMethod("method", listOf(assignA, assignC, assignB).map { JimpleNode(it) })
        }.sootClass.methods.last()

        assertThat(jimpleMethod.parameterTypes).containsExactly(IntType.v())
        assertThat(jimpleMethod.activeBody.parameterLocals.map { it.name }).containsExactly(b.name)
        assertThat(jimpleMethod.activeBody.locals.map { it.name })
            .contains(a.name, b.name, c.name)
    }

    it("should generate a method with all the locals used") {
        val a = Jimple.v().newLocal("a", BooleanType.v())
        val b = Jimple.v().newLocal("b", BooleanType.v())
        val c = Jimple.v().newLocal("c", BooleanType.v())

        val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAndExpr(a, b))

        val jimpleMethod = ClassGenerator("clazz").apply {
            generateMethod("method", listOf(assignC).map { JimpleNode(it) })
        }.sootClass.methods.last()

        assertThat(jimpleMethod.activeBody.locals.map { it.name }).contains(a.name, b.name, c.name)
    }

    it("should generate a method with return type void if no return is present") {
        val c = Jimple.v().newLocal("c", BooleanType.v())

        val assignC = Jimple.v().newAssignStmt(c, IntConstant.v(10))

        val jimpleMethod = ClassGenerator("testClass").apply {
            generateMethod("method", listOf(assignC).map { JimpleNode(it) })
        }.sootClass.methods.last()

        assertThat(jimpleMethod.returnType).isEqualTo(VoidType.v())
    }

    it("should generate a method with return type boolean if last statement is return boolean") {
        val c = Jimple.v().newLocal("c", BooleanType.v())

        val assignC = Jimple.v().newAssignStmt(c, IntConstant.v(10))
        val returnC = Jimple.v().newReturnStmt(c)

        val jimpleMethod = ClassGenerator("klazz").apply {
            generateMethod("method", listOf(assignC, returnC).map { JimpleNode(it) })
        }.sootClass.methods.last()

        assertThat(jimpleMethod.returnType).isEqualTo(c.type)
    }

    it("should generate a method with custom return type if last statement is custom return type") {
        val c = Jimple.v().newLocal("c", RefType.v("myClass"))

        val assignC = Jimple.v().newAssignStmt(c, IntConstant.v(10))
        val returnC = Jimple.v().newReturnStmt(c)

        val jimpleMethod = ClassGenerator("clasz").apply {
            generateMethod("method", listOf(assignC, returnC).map { JimpleNode(it) })
        }.sootClass.methods.last()

        assertThat(jimpleMethod.returnType).isEqualTo(c.type)
    }

    it("should generate a method with Object as return type if last statement returns null") {
        val returnStmt = Jimple.v().newReturnStmt(NullConstant.v())

        val jimpleMethod = ClassGenerator("myClass").apply {
            generateMethod("method", listOf(JimpleNode(returnStmt)))
        }.sootClass.methods.last()

        assertThat(jimpleMethod.returnType).isEqualTo(RefType.v("java.lang.Object"))
    }

    it("should generate a method with only statements before return") {
        val c = Jimple.v().newLocal("c", RefType.v("myClass"))

        val assignC = Jimple.v().newAssignStmt(c, IntConstant.v(10))
        val returnC = Jimple.v().newReturnStmt(c)
        val assignCAgain = Jimple.v().newAssignStmt(c, IntConstant.v(20))

        val jimpleMethod = ClassGenerator("testTestTest").apply {
            generateMethod("method", listOf(assignC, returnC, assignCAgain).map { JimpleNode(it) })
        }.sootClass.methods.last()

        assertThat(jimpleMethod.activeBody.units).hasSize(3)
    }

    describe("replacing missing targets") {
        it("should replace now-missing targets in goto statements") {
            val value = Jimple.v().newLocal("value", RefType.v("myClass"))

            val missingReturnStmt = Jimple.v().newReturnStmt(value)
            val returnStmt = Jimple.v().newReturnStmt(value)
            val gotoStmt = Jimple.v().newGotoStmt(missingReturnStmt)

            ClassGenerator("test").apply {
                generateMethod("method", listOf(gotoStmt, returnStmt).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(JimpleNode(gotoStmt.target as Stmt).equivTo(JimpleNode(returnStmt))).isTrue()
        }

        it("should replace now-missing targets in if statements") {
            val value = Jimple.v().newLocal("value", RefType.v("myClass"))
            val ifCondition = Jimple.v().newConditionExprBox(JEqExpr(value, value)).value

            val missingReturnStmt = Jimple.v().newReturnStmt(value)
            val returnStmt = Jimple.v().newReturnStmt(value)
            val ifStmt = Jimple.v().newIfStmt(ifCondition, missingReturnStmt)

            ClassGenerator("test").apply {
                generateMethod("method", listOf(ifStmt, returnStmt).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(JimpleNode(ifStmt.target as Stmt).equivTo(JimpleNode(returnStmt))).isTrue()
        }

        it("should replace now-missing targets in switch statements") {
            val value = Jimple.v().newLocal("value", RefType.v("myClass"))

            val missingReturnStmt = Jimple.v().newReturnStmt(value)
            val returnStmt = Jimple.v().newReturnStmt(value)
            val switchStmt = Jimple.v().newLookupSwitchStmt(
                value,
                listOf(IntConstant.v(1), IntConstant.v(2)),
                listOf(missingReturnStmt, returnStmt),
                missingReturnStmt
            )

            ClassGenerator("test").apply {
                generateMethod("method", listOf(switchStmt, returnStmt).map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(JimpleNode(switchStmt.targets[0] as Stmt).equivTo(JimpleNode(returnStmt))).isTrue()
            assertThat(JimpleNode(switchStmt.defaultTarget as Stmt).equivTo(JimpleNode(returnStmt))).isTrue()
        }
    }

    describe("duplication of statements") {
        it("should replace all statements with new instances") {
            val a = Jimple.v().newLocal("a", CharType.v())
            val b = Jimple.v().newLocal("b", CharType.v())
            val c = Jimple.v().newLocal("c", CharType.v())

            val assignA = Jimple.v().newAssignStmt(a, StringConstant.v("hello"))
            val assignB = Jimple.v().newAssignStmt(b, StringConstant.v("world"))
            val assignC = Jimple.v().newAssignStmt(c, Jimple.v().newAddExpr(a, b))
            val statements = listOf(assignA, assignB, assignC)

            val jimpleMethod = ClassGenerator("asdf").apply {
                generateMethod("method", statements.map { JimpleNode(it) })
            }.sootClass.methods.last()

            assertThat(jimpleMethod.activeBody.units).doesNotContainAnyElementsOf(statements)
        }

        it("should replace statement target instances in goto statements") {
            val value = Jimple.v().newLocal("value", RefType.v("myClass"))

            val returnStmt = Jimple.v().newReturnStmt(value)
            val gotoStmt = Jimple.v().newGotoStmt(returnStmt)

            val method = ClassGenerator("test").apply {
                generateMethod("method", listOf(gotoStmt, returnStmt).map { JimpleNode(it) })
            }.sootClass.methods.last()

            val newGotoStmt = method.activeBody.units.elementAt(1) as GotoStmt
            assertThat(newGotoStmt.target)
                .isNotEqualTo(returnStmt)
                .isEqualToComparingFieldByFieldRecursively(returnStmt)
        }

        it("should replace statement target instances correctly in a loop") {
            val ifStmt = Jimple.v().newIfStmt(
                Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(0)),
                Jimple.v().newNopStmt()
            )
            ifStmt.setTarget(ifStmt)
            val gotoStmt = Jimple.v().newGotoStmt(ifStmt)

            val method = ClassGenerator("test").apply {
                generateMethod("method", listOf(ifStmt, gotoStmt).map { JimpleNode(it) })
            }.sootClass.methods.last()

            val newIfStmt = method.activeBody.units.first() as IfStmt
            assertThat(newIfStmt.target).isEqualTo(newIfStmt)
        }

        it("should replace statement target instances in if statements") {
            val value = Jimple.v().newLocal("value", RefType.v("myClass"))
            val ifCondition = Jimple.v().newConditionExprBox(JEqExpr(value, value)).value

            val returnStmt = Jimple.v().newReturnStmt(value)
            val ifStmt = Jimple.v().newIfStmt(ifCondition, returnStmt)

            val method = ClassGenerator("test").apply {
                generateMethod("method", listOf(ifStmt, returnStmt).map { JimpleNode(it) })
            }.sootClass.methods.last()

            val newIfStmt = method.activeBody.units.elementAt(1) as IfStmt
            assertThat(newIfStmt.target)
                .isNotEqualTo(returnStmt)
                .isEqualToComparingFieldByFieldRecursively(returnStmt)
        }

        it("should replace statement target instances in switch statements") {
            val value = Jimple.v().newLocal("value", RefType.v("myClass"))

            val returnStmt = Jimple.v().newReturnStmt(value)
            val switchStmt = Jimple.v().newLookupSwitchStmt(
                value,
                listOf(IntConstant.v(1), IntConstant.v(2)),
                listOf(returnStmt, returnStmt),
                returnStmt
            )

            val method = ClassGenerator("test").apply {
                generateMethod("method", listOf(switchStmt, returnStmt).map { JimpleNode(it) })
            }.sootClass.methods.last()

            val newSwitchStmt = method.activeBody.units.elementAt(1) as SwitchStmt
            assertThat(newSwitchStmt.targets[0])
                .isNotEqualTo(returnStmt)
                .isEqualToComparingFieldByFieldRecursively(returnStmt)
            assertThat(newSwitchStmt.defaultTarget)
                .isNotEqualTo(returnStmt)
                .isEqualToComparingFieldByFieldRecursively(returnStmt)
        }
    }

    it("should not add a local twice if it is assigned twice") {
        val local = Jimple.v().newLocal("local", IntType.v())
        val userA = Jimple.v().newAssignStmt(local, IntConstant.v(48))
        val userB = Jimple.v().newAssignStmt(local, IntConstant.v(86))

        val method = ClassGenerator("test").apply {
            generateMethod("method", listOf(userA, userB).map { JimpleNode(it) })
        }.sootClass.methods.last()

        assertThat(method.retrieveActiveBody().locals).containsExactly(local)
    }

    describe("initialization of locals") {
        it("should add an initialization statement for all primitive types") {
            val booleanLocal = Jimple.v().newLocal("booleanLocal", IntType.v())
            val byteLocal = Jimple.v().newLocal("byteLocal", IntType.v())
            val charLocal = Jimple.v().newLocal("charLocal", IntType.v())
            val doubleLocal = Jimple.v().newLocal("doubleLocal", DoubleType.v())
            val floatLocal = Jimple.v().newLocal("floatLocal", FloatType.v())
            val intLocal = Jimple.v().newLocal("intLocal", IntType.v())
            val longLocal = Jimple.v().newLocal("longLocal", LongType.v())
            val shortLocal = Jimple.v().newLocal("shortLocal", IntType.v())

            val method = ClassGenerator("test").apply {
                generateMethod("method", listOf(
                    Jimple.v().newAssignStmt(booleanLocal, IntConstant.v(0)),
                    Jimple.v().newAssignStmt(byteLocal, IntConstant.v(0)),
                    Jimple.v().newAssignStmt(charLocal, IntConstant.v(0)),
                    Jimple.v().newAssignStmt(doubleLocal, DoubleConstant.v(0.0)),
                    Jimple.v().newAssignStmt(floatLocal, FloatConstant.v(0f)),
                    Jimple.v().newAssignStmt(intLocal, IntConstant.v(0)),
                    Jimple.v().newAssignStmt(longLocal, LongConstant.v(0)),
                    Jimple.v().newAssignStmt(shortLocal, IntConstant.v(0))
                ).map { JimpleNode(it) })
            }.sootClass.methods.last()
            val methodStatements = method.retrieveActiveBody().units

            assertThat(methodStatements).filteredOn { it is AssignStmt && it.leftOp.type is IntType }.hasSize(10)
            assertThat(methodStatements).filteredOn { it is AssignStmt && it.leftOp.type is DoubleType }.hasSize(2)
            assertThat(methodStatements).filteredOn { it is AssignStmt && it.leftOp.type is FloatType }.hasSize(2)
            assertThat(methodStatements).filteredOn { it is AssignStmt && it.leftOp.type is LongType }.hasSize(2)
        }

        it("should add an initialization statement for an object type") {
            val local = Jimple.v().newLocal("local", NullType.v())

            val method = ClassGenerator("test").apply {
                generateMethod("method", listOf(JimpleNode(Jimple.v().newAssignStmt(local, NullConstant.v()))))
            }.sootClass.methods.last()
            val methodStatements = method.retrieveActiveBody().units

            assertThat(methodStatements).filteredOn { it is AssignStmt && it.leftOp.type is NullType }.hasSize(2)
        }
    }
})
