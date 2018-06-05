package org.cafejojo.schaapi.githubtestreporter

import java.util.ArrayDeque
import java.util.Deque
import org.json.JSONObject

internal class JsonObjectBuilder {
    private val deque: Deque<JSONObject> = ArrayDeque()

    fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
        deque.push(JSONObject())
        this.build()
        return deque.pop()
    }

    @Suppress("FunctionMinLength") // Provides expressive 'DSL'
    infix fun <T> String.to(value: T) {
        deque.peek().put(this, value)
    }
}

internal fun json(of: String = "", build: JsonObjectBuilder.() -> Unit) = JsonObjectBuilder().json(build)
