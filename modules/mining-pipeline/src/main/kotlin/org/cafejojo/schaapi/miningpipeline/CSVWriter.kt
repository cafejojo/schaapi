package org.cafejojo.schaapi.miningpipeline

import mu.KLogging
import org.cafejojo.schaapi.models.Node
import java.io.File
import java.io.FileWriter

class CSVWriter<N : Node>(private val output: File) {
    companion object : KLogging()

    fun writeGraphSizes(graphs: List<N>) {
        val graphSizeFile = File(output, "graphSize.csv")
        logger.debug { "Writing graph sizes to ${graphSizeFile.absolutePath}." }

        val fileWriter = FileWriter(graphSizeFile)
        val graphSizes = mutableMapOf<Int, Int>()

        graphs.forEach { graphSizes[it.count()] = graphSizes[it.count()]?.inc() ?: 1 }

        fileWriter.write("graph_size,count\n")
        graphSizes.forEach { fileWriter.write("${it.key},${it.value}\n") }

        fileWriter.close()
        logger.debug { "Wrote pattern lengths to ${graphSizeFile.absolutePath}." }
    }

    fun writePatternLengths(patterns: List<Pattern<N>>) {
        val patternsFile = File(output, "patterns.csv")
        logger.debug { "Writing pattern lengths to ${patternsFile.absolutePath}." }

        val fileWriter = FileWriter(patternsFile)
        val patternLengths = mutableMapOf<Int, Int>()

        patterns.forEach { patternLengths[it.size] = patternLengths[it.size]?.inc() ?: 1 }

        fileWriter.write("pattern_length,count\n")
        patternLengths.forEach { fileWriter.write("${it.key},${it.value}\n") }

        fileWriter.close()
        logger.debug { "Wrote pattern lengths to ${patternsFile.absolutePath}." }
    }

    fun writeFilteredPatternLengths(patterns: List<Pattern<N>>) {
        val filteredPatternsFile = File(output, "filteredPatterns.csv")
        logger.debug { "Writing filtered pattern lengths to ${filteredPatternsFile.absolutePath}." }

        val fileWriter = FileWriter(filteredPatternsFile)
        val patternLengths = mutableMapOf<Int, Int>()

        patterns.forEach { patternLengths[it.size] = patternLengths[it.size]?.inc() ?: 1 }

        fileWriter.write("pattern_length,count\n")
        patternLengths.forEach { fileWriter.write("${it.key},${it.value}\n") }

        fileWriter.close()
        logger.debug { "Wrote filtered pattern lengths to ${filteredPatternsFile.absolutePath}." }
    }

    fun writeSequenceLengths(patterns: List<List<N>>) {
        val sequencesFile = File(output, "sequences.csv")
        logger.debug { "Writing sequence lengths to ${sequencesFile.absolutePath}." }

        val fileWriter = FileWriter(sequencesFile)
        val patternLengths = mutableMapOf<Int, Int>()

        patterns.forEach { patternLengths[it.size] = patternLengths[it.size]?.inc() ?: 1 }

        fileWriter.write("sequence_length,count\n")
        patternLengths.forEach { fileWriter.write("${it.key},${it.value}\n") }

        fileWriter.close()
        logger.debug { "Wrote sequence lengths to ${sequencesFile.absolutePath}." }
    }
}
