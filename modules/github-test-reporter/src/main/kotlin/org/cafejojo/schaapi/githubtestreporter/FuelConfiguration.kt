package org.cafejojo.schaapi.githubtestreporter

import com.github.kittinunf.fuel.core.FuelManager
import org.springframework.context.annotation.Configuration

@Configuration
internal open class FuelConfiguration(customHttpClient: CustomHttpClient) {
    init {
        FuelManager.instance.client = customHttpClient
    }
}
