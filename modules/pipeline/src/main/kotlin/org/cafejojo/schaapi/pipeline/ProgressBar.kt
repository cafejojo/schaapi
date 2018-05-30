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
     * Returns true iff the progress bar indicates that the process is done.
     */
    fun isDone(): Boolean
}

/**
 * A plain, thread-safe [ProgressBar].
 */
class FlatProgressBar(@Volatile private var target: Int, @Volatile private var progress: Int) : ProgressBar {
    @Synchronized
    override fun getTarget() = target

    /**
     * Updates the target value.
     *
     * @param newTarget the new target value
     */
    @Synchronized
    fun setTarget(newTarget: Int) {
        if (isDone()) throw IllegalStateException("Cannot change the progress once the process has completed.")
        require(newTarget >= progress) { "Target cannot be smaller than current progress, $progress." }

        target = newTarget
    }

    @Synchronized
    override fun getProgress() = progress

    /**
     * Updates the progress value.
     *
     * @param newProgress the new progress value
     */
    @Synchronized
    fun setProgress(newProgress: Int) {
        if (isDone()) throw IllegalStateException("Cannot change the target once the process has completed.")
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
        setProgress(progress + 1)
        return progress
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

    override fun isDone() = subProgressBars.all { it.isDone() }
}
