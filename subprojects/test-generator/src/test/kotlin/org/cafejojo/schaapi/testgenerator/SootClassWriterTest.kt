package org.cafejojo.schaapi.testgenerator

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import soot.Scene
import java.nio.file.Paths
import java.util.*
import javax.xml.bind.DatatypeConverter

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

            SootClassWriter.writeToFile(Scene.v().makeSootClass(testClassName), classOutputDirectory.path)

            assertThat(Paths.get(classOutputDirectory.absolutePath, "$testClassName.class")).exists()
        }

        it("outputs two class files in the root directory") {
            val testClassName1 = "MyFirstTestClass"
            val testClassName2 = "MySecondTestClass"

            SootClassWriter.writeToFile(Scene.v().makeSootClass(testClassName1), classOutputDirectory.path)
            SootClassWriter.writeToFile(Scene.v().makeSootClass(testClassName2), classOutputDirectory.path)

            assertThat(Paths.get(classOutputDirectory.absolutePath, "$testClassName1.class")).exists()
            assertThat(Paths.get(classOutputDirectory.absolutePath, "$testClassName2.class")).exists()
        }

        it("outputs a class file in a directory structure") {
            val testClassName = "org.test.MyTestClass"

            SootClassWriter.writeToFile(Scene.v().makeSootClass(testClassName), classOutputDirectory.path)

            assertThat(Paths.get(classOutputDirectory.absolutePath, "org", "test", "MyTestClass.class")).exists()
        }

        it("outputs the same bytecode for a simple class, consistently") {
            val testClassName = "MyTestClass"

            val byteOutputStream = ByteOutputStream()
            SootClassWriter.writeToOutputStream(Scene.v().makeSootClass(testClassName), byteOutputStream)

            assertThat(DatatypeConverter.printBase64Binary(byteOutputStream.bytes))
                    .isEqualTo("yv66vgAAAC4AAwcAAgEAC015VGVzdENsYXNzACAAAQ" +
                            Collections.nCopies(1324, "A").joinToString(separator = "") { it } +
                            "==")
        }
    }
})
