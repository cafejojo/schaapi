package org.cafejojo.schaapi.validationpipeline.githubinteractor.githubapi

import org.json.JSONObject
import java.util.ArrayDeque
import java.util.Deque

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

@Suppress("UNUSED_PARAMETER") // Gives context to its usage
internal fun json(of: String = "", build: JsonObjectBuilder.() -> Unit) = JsonObjectBuilder().json(build)
