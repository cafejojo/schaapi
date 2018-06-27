package org.cafejojo.schaapi.miningpipeline

import mu.KLogging
import org.cafejojo.schaapi.models.Node
import java.io.File
import java.io.FileWriter

/**
 * CSV writer that writes the desired data to separate csv files under a data directory.
 *
 * @param output file to which to write the output
 */
class CsvWriter<N : Node>(output: File) {
    companion object : KLogging()

    private val dataFile: File = output.resolve("data/").apply { mkdirs() }

    /**
     * Writes graph sizes to file.
     *
     * @param graphs graphs whose node counts will be written to file
     */
    fun writeGraphSizes(graphs: List<N>) {
        val graphSizeFile = File(dataFile, "graphSize.csv")
        logger.debug { "Writing graph sizes to ${graphSizeFile.absolutePath}." }
        writeToFile(graphSizeFile, graphs, { graph: N -> graph.count() }, "graph_size")
        logger.debug { "Wrote pattern lengths to ${graphSizeFile.absolutePath}." }
    }

    /**
     * Writes pattern lengths to file.
     *
     * @param patterns patterns whose lengths will be written to file
     */
    fun writePatternLengths(patterns: List<Pattern<N>>) {
        val patternsFile = File(dataFile, "patterns.csv")
        logger.debug { "Writing pattern lengths to ${patternsFile.absolutePath}." }
        writeToFile(patternsFile, patterns, { pattern: Pattern<N> -> pattern.size }, "pattern_length")
        logger.debug { "Wrote pattern lengths to ${patternsFile.absolutePath}." }
    }

    /**
     * Writes filtered pattern lengths to file.
     *
     * @param patterns patterns whose lengths will be written to file
     */
    fun writeFilteredPatternLengths(patterns: List<Pattern<N>>) {
        val filteredPatternsFile = File(dataFile, "filteredPatterns.csv")
        logger.debug { "Writing filtered pattern lengths to ${filteredPatternsFile.absolutePath}." }
        writeToFile(filteredPatternsFile, patterns, { pattern: Pattern<N> -> pattern.size }, "pattern_length")
        logger.debug { "Wrote filtered pattern lengths to ${filteredPatternsFile.absolutePath}." }
    }

    private fun <P> writeToFile(output: File, items: List<P>, mapToInt: (P) -> Int, type: String) =
        FileWriter(output).use { fileWriter ->
            fileWriter.write("$type,count\n")
            items
                .groupBy(mapToInt)
                .toSortedMap()
                .forEach { fileWriter.write("${it.key},${it.value.size}\n") }
        }
}
