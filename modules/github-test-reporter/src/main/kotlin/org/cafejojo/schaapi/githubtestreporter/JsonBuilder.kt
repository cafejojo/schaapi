package org.cafejojo.schaapi.githubtestreporter

import java.util.ArrayDeque
import java.util.Deque
import org.json.JSONObject

internal fun json(of: String = "", build: JsonObjectBuilder.() -> Unit): JSONObject {
    return JsonObjectBuilder().json(build)
}

internal class JsonObjectBuilder {
    private val deque: Deque<JSONObject> = ArrayDeque()

    fun json(build: JsonObjectBuilder.() -> Unit): JSONObject {
        deque.push(JSONObject())
        this.build()
        return deque.pop()
    }

    infix fun <T> String.to(value: T) {
        deque.peek().put(this, value)
    }
}
