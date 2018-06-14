package org.cafejojo.schaapi.validationpipeline.cijob

import org.cafejojo.schaapi.validationpipeline.events.ValidationRequestReceivedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Starts up the execution of a CI job.
 */
@Component
class CIJobInitiator {
    /**
     * Listens to [ValidationRequestReceivedEvent] events.
     */
    @EventListener
    fun handleValidateRequestReceivedEvent(event: ValidationRequestReceivedEvent) {
        println("Received event $event")
    }
}
