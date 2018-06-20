package org.cafejojo.schaapi.validationpipeline.events

import org.cafejojo.schaapi.validationpipeline.TestResults

/**
 * Event that can be broadcast after receiving a CI job run is completed.
 */
data class CIJobCompletedEvent(val testResults: TestResults)
