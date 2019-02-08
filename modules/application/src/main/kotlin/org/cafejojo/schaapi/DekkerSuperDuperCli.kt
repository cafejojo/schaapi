package org.cafejojo.schaapi

import java.io.File

val libs = mapOf(
    "commons-io" to Triple("commons-io", "commons-io", "2.4"),
    "commons-lang" to Triple("commons-lang", "commons-lang", "2.6"),
    "commons-logging" to Triple("commons-logging", "commons-logging", "1.2"),
    "easymock" to Triple("org.easymock", "easymock", "3.1"),
    "gson" to Triple("com.google.code.gson", "gson", "2.8.5"),
    "guava" to Triple("com.google.guava", "guava", "27.0.1-jre"),
    "jackson" to Triple("com.fasterxml.jackson.core", "jackson-databind", "2.9.8"),
    "log4j" to Triple("log4j", "log4j", "2.11.1"),
    "logback-core" to Triple("ch.qos.logback", "logback-core", "1.2.3"),
    "rxjava" to Triple("io.reactivex.rxjava2", "rxjava", "2.2.6")
)

fun main(myArgs: Array<String>) {
    val workDir = File(myArgs[0])

    println("gh/dir?")
    val pipe = readLine()
    require(pipe == "gh" || pipe == "dir") { "Unrecognised pipeline" }

    println("\nAvailable libraries:")
    println(libs.keys.joinToString("\n") { "* $it" })
    println("Which library?")
    val lib = readLine()
    require(libs.contains(lib)) { "Unrecognised library" }

    println("\njavajar/javamaven?")
    val libType = readLine()
    require(libType == "javajar" || libType == "javamaven") { "Unrecognised library type" }

    val libFile = when (libType) {
        "javajar" -> File(File(File(workDir, lib), "library"), "${libs[lib]!!.second}-${libs[lib]!!.third}.jar")
        "javamaven" -> File(File(workDir, lib), "library")
        else -> error("Unrecognised library type")
    }

    println("\nPattern support? [default = 5]")
    val support = readLine().let { if (it == null || it.isBlank()) "5" else it }

    when (pipe) {
        "gh" -> {
            println("\nUser project count? [default = 400]")
            val projectCount = readLine().let { if (it == null || it.isBlank()) "400" else it }

            main(args = arrayOf(
                "github",

                "-o", File(File(workDir, lib), "output").absolutePath,
                "--delete_old_output",

                "-l", libFile.absolutePath,
                "--library_type", libType,

                "--github_oauth_token", "c0cbec3e6ddc091f3358da2a0df08155061403c5",
                "--library_group_id", libs[lib]!!.first,
                "--library_artifact_id", libs[lib]!!.second,
                "--library_version", libs[lib]!!.third,
                "--max_projects", projectCount,

                "--pattern_detector_minimum_count", support,

                "--test_generator_disable_output",
                "--test_generator_parallel",
                "--test_generator_timeout", "30"
            ))
        }
        "dir" -> {
            main(args = arrayOf(
                "directory",

                "-o", File(File(workDir, lib), "output").absolutePath,
                "--delete_old_output",

                "-l", libFile.absolutePath,
                "--library_type", libType,

                "-u", File(File(workDir, lib), "users").absolutePath,
                "--skip_user_compile",

                "--pattern_detector_minimum_count", support,

                "--test_generator_disable_output",
                "--test_generator_parallel",
                "--test_generator_timeout", "30"
            ))
        }
    }
}

