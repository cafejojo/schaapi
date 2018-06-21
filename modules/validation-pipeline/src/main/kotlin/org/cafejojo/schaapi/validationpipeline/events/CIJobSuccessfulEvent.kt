package org.cafejojo.schaapi.validationpipeline.events

import org.cafejojo.schaapi.validationpipeline.TestResults

/**
 * Event that can be broadcast after a CI job run is completed successfully.
 */
data class CIJobSuccessfulEvent(val testResults: TestResults)
