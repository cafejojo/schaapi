package org.cafejojo.schaapi.validationpipeline.githubtestreporter

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Stack
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

internal fun mockHttpClient(vararg jsonResponses: JSONObject, code: Int = 200): List<Pair<Future<Request>, Response>> {
    val responses = jsonResponses.map { json ->
        mock<Response> {
            on { statusCode } doReturn code
            on { dataStream } doReturn ByteArrayInputStream(json.toString().toByteArray())
        }
    }

    val requestsAndResponses = responses.map { CompletableFuture<Request>() to it }
    val requestsAndResponsesStack = Stack<Pair<CompletableFuture<Request>, Response>>().apply {
        addAll(requestsAndResponses.asReversed())
    }

    FuelConfiguration(mock {
        on { executeRequest(any()) } doAnswer { answer ->
            val (request, response) = requestsAndResponsesStack.pop()

            request.complete(answer.getArgument(0))
            response
        }
    })

    return requestsAndResponses
}

internal fun Request.bodyContents() = ByteArrayOutputStream().let { outputStream ->
    bodyCallback?.invoke(request, outputStream, 0)

    String(outputStream.toByteArray(), StandardCharsets.UTF_8)
}
