package org.cafejojo.schaapi.testgenerator

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Scene
import java.nio.file.Paths

internal class SootClassWriterTest : Spek({
    val classPath = Paths.get(SootClassWriterTest::class.java.getResource("../../../../").toURI()).toString()
    val classOutputDirectory = Paths.get(classPath, "class-writer-test/").toFile()

    fun deleteClassFileOutput() {
        if (classOutputDirectory.exists()) {
            classOutputDirectory.deleteRecursively()
        }
    }

    beforeGroup {
        deleteClassFileOutput()
    }

    afterEachTest {
        deleteClassFileOutput()
    }

    describe("writing SootClasses to .class files") {
        it("outputs a class file in the root directory") {
            val testClassName = "MyTestClass"

            val sootClassWriter = SootClassWriter(classOutputDirectory.path)
            sootClassWriter.writeFile(Scene.v().makeSootClass(testClassName))

            assertThat(Paths.get(classOutputDirectory.absolutePath, "$testClassName.class")).exists()
        }

        it("outputs two class files in the root directory") {
            val testClassName1 = "MyFirstTestClass"
            val testClassName2 = "MySecondTestClass"

            val sootClassWriter = SootClassWriter(classOutputDirectory.path)
            sootClassWriter.writeFile(Scene.v().makeSootClass(testClassName1))
            sootClassWriter.writeFile(Scene.v().makeSootClass(testClassName2))

            assertThat(Paths.get(classOutputDirectory.absolutePath, "$testClassName1.class")).exists()
            assertThat(Paths.get(classOutputDirectory.absolutePath, "$testClassName2.class")).exists()
        }

        it("outputs a class file in a directory structure") {
            val testClassName = "org.test.MyTestClass"

            val sootClassWriter = SootClassWriter(classOutputDirectory.path)
            sootClassWriter.writeFile(Scene.v().makeSootClass(testClassName))

            assertThat(Paths.get(classOutputDirectory.absolutePath, "org", "test", "MyTestClass.class")).exists()
        }
    }
})
