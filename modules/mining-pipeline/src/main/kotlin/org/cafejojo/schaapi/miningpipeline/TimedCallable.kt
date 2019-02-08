package org.cafejojo.schaapi.miningpipeline

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * A [Callable] that is cancelled if it takes too long.
 *
 * @param V the return type of the task
 * @property timeout the number of seconds after which the task is cancelled if it is not done
 * @property task the task to execute within the time limit
 */
class TimedCallable<V>(private val timeout: Long, private val task: () -> V) : Callable<V> {
    /**
     * Executes [task].
     *
     * @return the output of [task] if it executed within the timeout, or `null` otherwise
     */
    override fun call(): V? {
        val executor = Executors.newSingleThreadExecutor()

        val results: List<Future<V>>
        try {
            results = executor.invokeAll(
                mutableListOf(Callable<V>(task)),
                timeout, TimeUnit.SECONDS
            )
        } catch (e: InterruptedException) {
            return null
        } finally {
            executor.shutdown()
        }

        return if (results[0].isCancelled) null else results[0].get()
    }
}
