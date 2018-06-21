package org.cafejojo.schaapi.validationpipeline.events

import org.cafejojo.schaapi.validationpipeline.CIJobException

/**
 * Event that can be broadcast after a CI job run has failed.
 */
data class CIJobFailedEvent(val exception: CIJobException)
