package org.cafejojo.schaapi.miningpipeline.patternfilter.jimple.libraryusagefilters

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.cafejojo.schaapi.models.project.JavaProject
import soot.Modifier
import soot.SootClass
import soot.SootMethod
import soot.jimple.StaticInvokeExpr
import java.io.File

internal const val NON_LIBRARY_CLASS = "java.lang.String"
internal const val USER_CLASS =
    "org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.users.SimpleTest"
internal const val LIBRARY_CLASS =
    "org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1"

internal fun constructInvokeExprMock(declaringClassName: String): StaticInvokeExpr {
    val clazz = SootClass(declaringClassName, Modifier.PUBLIC)
    val method = mock<SootMethod> {
        on { declaringClass } doReturn clazz
    }
    return mock {
        on { getMethod() } doReturn method
    }
}

internal data class TestProject(
    override var classpath: String = "",
    override var classNames: Set<String> = emptySet()
) : JavaProject {
    override val classDir: File = File(".")
    override val dependencyDir: File = File(".")
    override var dependencies: Set<File> = emptySet()
    override val projectDir: File = File(".")
    override var classes: Set<File> = emptySet()
}

internal val libraryClasses = setOf(
    "org.cafejojo.schaapi.miningpipeline.usagegraphgenerator.jimple.testclasses.library.Object1"
)
internal val libraryProject = TestProject(classNames = libraryClasses)
