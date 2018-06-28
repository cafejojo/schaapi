package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import java.io.File
import kotlin.system.exitProcess

/**
 * Runs the complete first phase of the Schaapi pipeline.
 *
 * @param args the path to the output directory, the path to the library project, and the paths to the user projects
 */
fun main(args: Array<String>) {
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

abstract class CommandLineInterface {
    lateinit var outputDir: File
    lateinit var libraryDir: File

    val snippets: MutableList<Snippet> = mutableListOf()

    fun run(args: Array<String>) {
        val cmd = parse(args)
        snippets.forEach { it.setUp(cmd) }

        outputDir = File(cmd.getOptionValue('o')).apply { mkdirs() }
        libraryDir = File(cmd.getOptionValue('l'))
    }

    abstract fun run(cmd: CommandLine)

    private fun buildOptions(): Options = Options()
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

    private fun parse(args: Array<String>): CommandLine {
        val options = buildOptions()
        snippets.forEach { it.addOptionsTo(options) }

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
