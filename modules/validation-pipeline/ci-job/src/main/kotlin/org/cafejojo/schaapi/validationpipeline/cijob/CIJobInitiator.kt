package org.cafejojo.schaapi.validationpipeline.cijob

import org.cafejojo.schaapi.validationpipeline.CIJobException
import org.cafejojo.schaapi.validationpipeline.events.CIJobFailedEvent
import org.cafejojo.schaapi.validationpipeline.events.CIJobSucceededEvent
import org.cafejojo.schaapi.validationpipeline.events.ValidationRequestReceivedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Starts up the execution of a CI job.
 */
@Component
class CIJobInitiator(private val publisher: ApplicationEventPublisher) {
    companion object {
        val executorService: ExecutorService = Executors.newFixedThreadPool(5)
    }

    /**
     * Listens to [ValidationRequestReceivedEvent] events.
     */
    @EventListener
    @Suppress("InstanceOfCheckForException")
    fun handleValidateRequestReceivedEvent(event: ValidationRequestReceivedEvent) =
        try {
            executorService.submit(CIJob(event.identifier, event.directory, event.downloadUrl))
                .get()
                .let { testResults -> publisher.publishEvent(CIJobSucceededEvent(testResults)) }
        } catch (exception: ExecutionException) {
            exception.cause.let {
                if (it !is CIJobException) throw exception

                publisher.publishEvent(CIJobFailedEvent(it))
            }
        }
}
