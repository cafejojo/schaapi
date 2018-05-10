package org.cafejojo.schaapi.testgenerator

import org.evosuite.EvoSuite

object EvoSuiteRunner {
    fun run(fullyQualifiedClassName: String, classPath: String, outputDirectory: String, generationTimeout: Int = 60) {
        val evosuite = EvoSuite()
        evosuite.parseCommandLine(
            arrayOf(
                "-class", fullyQualifiedClassName,
                "-base_dir", outputDirectory,
                "-projectCP", classPath,
                "-Dsearch_budget=$generationTimeout",
                "-Dstopping_condition=MaxTime"
            )
        )

//        val handler = ExternalProcessHandler()
//        handler.closeServer()

//        println(MasterServices.getInstance()
        println("yeeyeyeye")
//        println(Thread.getAllStackTraces().keys.size)
//        Thread.getAllStackTraces().keys.forEach {
//            it.join()
//        }
    }
}
