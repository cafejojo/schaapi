package org.cafejojo.schaapi.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * The web application.
 */
@SpringBootApplication
class WebApplication

/**
 * Starts the web application.
 */
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<WebApplication>(*args)
}
