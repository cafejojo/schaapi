package org.cafejojo.schaapi.githubtestreporter

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import org.springframework.context.annotation.Configuration

@Configuration
internal open class FuelConfiguration(httpClient: Client) {
    init {
        FuelManager.instance.client = httpClient
    }
}
