package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

/**
 * Runs the complete first phase of the Schaapi pipeline.
 *
 * @param args the path to the output directory, the path to the library project, and the paths to the user projects
 */
fun main(args: Array<String>) {
    printAsciiArt()
    if (args.isEmpty()) {
        KLogging().logger.error { "At least one argument expected." }
        exitProcess(-1)
    }

    val flavor = args[0]
    val remainingArgs = args.drop(1).toTypedArray()
    when (flavor) {
        "directory" -> DirectoryMiningCommandLineInterface().run(remainingArgs)
        "github" -> GitHubMiningCommandLineInterface().run(remainingArgs)
        else -> {
            KLogging().logger.error { "Unrecognized pipeline flavor: $flavor." }
            exitProcess(-1)
        }
    }
}

private fun printAsciiArt() = try {
    println(CommandLineInterface::class.java.getResource("/schaapi-ascii-art.txt")?.readText())
} catch (e: IOException) {
    println("Schaapi")
}

/**
 * A "flavor" of command-line interface (CLI) that runs the mining pipeline using a particular set of arguments.
 *
 * The behavior of a [CommandLineInterface] is determined by its [optionSets], which are mappings from command-line
 * arguments to actual pipeline components.
 */
@Suppress("LateinitUsage") // Values cannot be determined at initialization
abstract class CommandLineInterface {
    lateinit var outputDir: File
    lateinit var libraryDir: File

    val optionSets: MutableList<OptionSet> = mutableListOf()

    /**
     * Runs this CLI with the given arguments.
     *
     * @param args the arguments to run with
     */
    fun run(args: Array<String>) {
        val cmd = parse(args)
        optionSets.forEach { it.read(cmd) }

        outputDir = File(cmd.getOptionValue('o')).apply { mkdirs() }
        libraryDir = File(cmd.getOptionValue('l'))

        run(cmd)
    }

    /**
     * Runs this CLI with the given parser arguments.
     *
     * @param cmd the parsed arguments to run with
     */
    abstract fun run(cmd: CommandLine)

    /**
     * The options that are common to all CLIs.
     */
    private fun globalOptions(): Options = Options()
        .addOption(Option
            .builder("o")
            .longOpt("output_dir")
            .desc("The output directory.")
            .hasArg()
            .required()
            .build())
        .addOption(Option
            .builder("l")
            .longOpt("library_dir")
            .desc("The library directory.")
            .hasArg()
            .required()
            .build())

    /**
     * Parses [args] into a [CommandLine] object. If the parsing fails, the entire process is killed.
     *
     * @param args the arguments to parse
     * @return [args] parsed into a [CommandLine]
     */
    private fun parse(args: Array<String>): CommandLine {
        val options = globalOptions()
        optionSets.forEach { it.addOptionsTo(options) }

        val parser = DefaultParser()
        return try {
            parser.parse(options, args)
        } catch (e: ParseException) {
            println(e.message)
            printHelpMessage(options)
            exitProcess(-1)
        }
    }

    private fun printHelpMessage(options: Options) {
        val helpFormatter = HelpFormatter()
        helpFormatter.optionComparator = null
        helpFormatter.printHelp("schaapi", options, true)
    }
}
