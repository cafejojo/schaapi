package org.cafejojo.schaapi.miningpipeline

import mu.KLogging
import org.cafejojo.schaapi.models.Node
import java.io.File

/**
 * CSV writer that writes the desired data to separate CSV files under a data directory.
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
    fun writeGraphSizes(graphs: Iterable<N>) =
        writeToFile("graphSize.csv", graphs, { graph: N -> graph.count() }, "graph_size")

    /**
     * Writes pattern lengths to file.
     *
     * @param patterns patterns whose lengths will be written to file
     */
    fun writePatternLengths(patterns: Iterable<Pattern<N>>) =
        writeToFile("patterns.csv", patterns, { pattern: Pattern<N> -> pattern.size }, "pattern_length")

    /**
     * Writes filtered pattern lengths to file.
     *
     * @param patterns patterns whose lengths will be written to file
     */
    fun writeFilteredPatternLengths(patterns: Iterable<Pattern<N>>) =
        writeToFile("filteredPatterns.csv", patterns, { pattern: Pattern<N> -> pattern.size }, "pattern_length")

    private fun <P> writeToFile(output: String, items: Iterable<P>, mapToInt: (P) -> Int, type: String) =
        File(dataFile, output).writer().use { fileWriter ->
            fileWriter.write("$type,count\n")
            items
                .groupBy(mapToInt)
                .toSortedMap()
                .forEach { fileWriter.write("${it.key},${it.value.size}\n") }
        }
}
