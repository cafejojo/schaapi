package org.cafejojo.validationpipeline.testablesourcefinder

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import java.io.File

/**
 * Responsible for finding the source code of a method within a Java file.
 */
class MethodSourceRetriever(private val javaFile: File) {
    /**
     * Returns the source code of the method belonging to the given [methodName].
     *
     * @param methodName the name of the method
     * @return method source or null if the method could not be found
     */
    fun getSourceOf(methodName: String) =
        mutableListOf<String>().also { foundMethodSources ->
            JavaParser.parse(javaFile).accept(MethodFinder(methodName), foundMethodSources)
        }.firstOrNull()
}

private class MethodFinder(val methodName: String) : VoidVisitorAdapter<MutableList<String>>() {
    override fun visit(method: MethodDeclaration, results: MutableList<String>) {
        if (method.name.identifier == methodName && method.body.isPresent) {
            results.add(
                "// Variables with automatically generated values:\n"
                    + method.parameters.joinToString("") { "$it;\n" }
                    + "\n"
                    + "// Pattern:\n"
                    + method.body.get().toString()
            )
        }

        super.visit(method, results)
    }
}
