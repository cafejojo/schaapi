package org.cafejojo.validationpipeline.testablesourcefinder

/**
 * Responsible for finding pattern names within a test file.
 */
class PatternNameFinder(private val searchableSource: String) {
    private val query = Regex(".*Patterns\\.(pattern\\d+)\\(.*")

    /**
     * Finds all pattern names within a test file.
     */
    fun find() = searchableSource.lines().mapNotNull {
        query.find(it)?.destructured?.component1()
    }
}
