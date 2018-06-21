package org.cafejojo.schaapi.validationpipeline.events

import org.cafejojo.schaapi.validationpipeline.CIJobException
import org.cafejojo.schaapi.validationpipeline.CIJobProjectMetadata

/**
 * Event that can be broadcast after a CI job run has failed.
 */
data class CIJobFailedEvent(val exception: CIJobException, val metadata: CIJobProjectMetadata)
