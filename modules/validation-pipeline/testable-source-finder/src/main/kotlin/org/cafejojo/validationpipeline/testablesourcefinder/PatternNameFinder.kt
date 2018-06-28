package org.cafejojo.validationpipeline.testablesourcefinder

/**
 * Finds pattern names within a test file.
 */
class PatternNameFinder(private val searchableSource: String) {
    private val query = Regex(".*Patterns\\.(pattern\\d+)\\(.*")

    /**
     * Finds pattern names within a test file.
     */
    fun find() = searchableSource.lines().mapNotNull {
        query.find(it)?.destructured?.component1()
    }
}
