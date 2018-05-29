package org.cafejojo.schaapi.web

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Controller for the home page.
 */
@Controller
@EnableAutoConfiguration
class HomeController {
    /**
     * Returns the contents of the home page.
     */
    @RequestMapping("/")
    @ResponseBody
    @Suppress("FunctionOnlyReturningConstant")
    fun home() = "Welcome to Schaapi"
}
