package org.cafejojo.schaapi.validationpipeline.cijob

import org.cafejojo.schaapi.validationpipeline.events.CIJobCompletedEvent
import org.cafejojo.schaapi.validationpipeline.events.ValidationRequestReceivedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
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
    fun handleValidateRequestReceivedEvent(event: ValidationRequestReceivedEvent) =
        executorService.submit(CIJob(event.identifier, event.directory, event.downloadUrl))
            .get()
            .let { testResults -> publisher.publishEvent(CIJobCompletedEvent(testResults)) }
}
