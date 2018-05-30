package org.cafejojo.schaapi.pipeline

/**
 * Indicates the progress of some process.
 *
 * Neither the progress nor the value can be changed once they equal, to prevent a process from suddenly becoming
 * "unfinished".
 */
interface ProgressBar {
    /**
     * Returns the target progress of the process. If [getProgress] equals this number, the process is complete.
     */
    fun getTarget(): Int

    /**
     * Returns the progress as a number between 0 and [getTarget], inclusive.
     */
    fun getProgress(): Int

    /**
     * Applies the [mapper] to the normalized progress, which is the progress expressed as a number between 0 and 1,
     * inclusive.
     *
     * The mapper function can be used to display a different progress to create a different user experience.
     *
     * @param mapper maps the normalized progress to a number between 0 and 1, inclusive
     * @return the result of applying the [mapper] to the progress as a number between 0 and 1, inclusive
     */
    fun getNormalizedProgress(mapper: (Double) -> Double = { it }): Double

    /**
     * Returns true iff the progress bar indicates that the process is done.
     */
    fun isDone(): Boolean
}

/**
 * A plain, thread-safe [ProgressBar].
 */
class FlatProgressBar(@Volatile private var progress: Int, @Volatile private var target: Int) : ProgressBar {
    init {
        require(target >= progress) { "Target must be larger than current progress." }
        require(progress in 0..target) { "Progress must be between 0 and target, inclusive." }
    }

    @Synchronized
    override fun getTarget() = target

    /**
     * Updates the target value.
     *
     * @param newTarget the new target value
     */
    @Synchronized
    fun setTarget(newTarget: Int) {
        check(!isDone()) { "Cannot change the target once the process has completed." }
        require(newTarget >= progress) { "Target cannot be smaller than current progress, $progress." }

        target = newTarget
    }

    @Synchronized
    override fun getProgress() = progress

    @Synchronized
    override fun getNormalizedProgress(mapper: (Double) -> Double) = mapper(progress.toDouble() / target)

    /**
     * Updates the progress value.
     *
     * @param newProgress the new progress value
     */
    @Synchronized
    fun setProgress(newProgress: Int) {
        check(!isDone()) { "Cannot change the progress once the process has completed." }
        require(newProgress >= 0) { "Progress cannot be negative." }
        require(newProgress <= target) { "Progress cannot be higher than the target, $target." }

        progress = newProgress
    }

    /**
     * Increments the progress by one.
     *
     * @return the updated progress value
     */
    @Synchronized
    fun incrementProgress(): Int {
        check(!isDone()) { "Cannot increment the progress once the process has completed." }

        return ++progress
    }

    @Synchronized
    override fun isDone() = target == progress
}

/**
 * A [ProgressBar] of which the progress is determined by the contained [ProgressBar]s.
 */
class HierarchicalProgressBar(private val subProgressBars: Set<ProgressBar>) : ProgressBar {
    init {
        require(subProgressBars.isNotEmpty()) { "Hierarchical progress bar must contain at least one progress bar." }
    }

    override fun getTarget() = subProgressBars.sumBy { it.getTarget() }

    override fun getProgress() = subProgressBars.sumBy { it.getProgress() }

    override fun getNormalizedProgress(mapper: (Double) -> Double): Double {
        var progressSum = 0
        var targetSum = 0

        subProgressBars.forEach {
            synchronized(it) {
                progressSum += it.getProgress()
                targetSum += it.getTarget()
            }
        }

        return mapper(progressSum.toDouble() / targetSum)
    }

    override fun isDone() = subProgressBars.all { it.isDone() }
}
