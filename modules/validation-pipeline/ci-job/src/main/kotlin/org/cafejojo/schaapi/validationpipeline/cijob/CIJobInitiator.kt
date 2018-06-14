package org.cafejojo.schaapi.validationpipeline.cijob

import org.cafejojo.schaapi.validationpipeline.events.ValidateRequestReceivedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class CIJobInitiator {
    @EventListener
    fun handleValidateRequestReceivedEvent(event: ValidateRequestReceivedEvent) {
        println("Received event $event")
    }
}
