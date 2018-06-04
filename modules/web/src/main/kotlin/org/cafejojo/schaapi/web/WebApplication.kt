package org.cafejojo.schaapi.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * The web application.
 */
@SpringBootApplication
@ComponentScan(basePackages = ["org.cafejojo.schaapi.web", "org.cafejojo.schaapi.githubtestreporter"])
open class WebApplication

/**
 * Starts the web application.
 */
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<WebApplication>(*args)
}
