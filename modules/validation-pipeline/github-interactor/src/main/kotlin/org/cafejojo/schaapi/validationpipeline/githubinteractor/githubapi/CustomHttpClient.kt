/*
 * MIT License
 *
 * Copyright (c) 2017 Kittinun Vantasin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.cafejojo.schaapi.validationpipeline.githubinteractor.githubapi

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import org.springframework.stereotype.Service
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URLConnection
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

/**
 * This class temporarily solves an issue with Java's [HttpsURLConnection], which does not support PATCH requests.
 *
 * This class can be removed when either GitHub supports the `X-HTTP-Method-Override`, which is the recommended
 * solution for dealing with the missing PATCH HTTP verb in [HttpsURLConnection], or when [HttpsURLConnection] gets
 * support for PATCH.
 *
 * This class is a copy of [com.github.kittinunf.fuel.toolbox.HttpClient], with the check/replace for PATCH requests
 * removed.
 */
@Service
@Suppress("TooGenericExceptionCaught")
internal class CustomHttpClient(private val proxy: Proxy? = null) : Client {
    init {
        allowMethods("PATCH")
    }

    override fun executeRequest(request: Request): Response {
        val connection = establishConnection(request) as? HttpURLConnection
            ?: throw IllegalStateException("Connection invalid.")

        try {
            connection.apply {
                connectTimeout = request.timeoutInMillisecond
                readTimeout = request.timeoutReadInMillisecond
                doInput = true
                useCaches = false
                requestMethod = request.method.value
                instanceFollowRedirects = false

                for ((key, value) in request.headers) {
                    setRequestProperty(key, value)
                }

                setDoOutput(connection, request.method)
                setBodyIfDoOutput(connection, request)
            }

            val contentEncoding = connection.contentEncoding ?: ""

            return Response(
                url = request.url,
                headers = connection.headerFields.filterKeys { it != null },
                contentLength = connection.contentLength.toLong(),
                statusCode = connection.responseCode,
                responseMessage = connection.responseMessage.orEmpty(),
                dataStream = try {
                    val stream = connection.errorStream ?: connection.inputStream
                    if (contentEncoding.compareTo("gzip", true) == 0) GZIPInputStream(stream) else stream
                } catch (exception: IOException) {
                    connection.errorStream ?: connection.inputStream?.close()
                    ByteArrayInputStream(ByteArray(0))
                }
            )
        } catch (exception: Exception) {
            throw FuelError(exception, ByteArray(0), Response(request.url))
        } finally {
            // As per Android documentation, a connection that is not explicitly disconnected
            // will be pooled and reused!  So, don't close it as we need inputStream later!
            // connection.disconnect()
        }
    }

    private fun establishConnection(request: Request): URLConnection =
        if (proxy != null) request.url.openConnection(proxy) else request.url.openConnection()

    private fun setBodyIfDoOutput(connection: HttpURLConnection, request: Request) {
        val bodyCallback = request.bodyCallback
        if (bodyCallback != null && connection.doOutput) {
            val contentLength = bodyCallback(request, null, 0)

            if (request.type == Request.Type.UPLOAD)
                connection.setFixedLengthStreamingMode(contentLength.toInt())

            BufferedOutputStream(connection.outputStream).use {
                bodyCallback(request, it, contentLength)
            }
        }
    }

    private fun setDoOutput(connection: HttpURLConnection, method: Method) = when (method) {
        Method.GET, Method.DELETE, Method.HEAD -> connection.doOutput = false
        Method.POST, Method.PUT, Method.PATCH -> connection.doOutput = true
    }

    private fun allowMethods(vararg methods: String) {
        val methodsField = HttpURLConnection::class.java.getDeclaredField("methods").apply {
            isAccessible = true
        }

        Field::class.java.getDeclaredField("modifiers").apply {
            isAccessible = true
            setInt(methodsField, methodsField.modifiers and Modifier.FINAL.inv())
        }

        val newMethods =
            (methodsField.get(null) as? Array<*>)?.filterIsInstance<String>()?.toTypedArray()?.plus(methods)

        methodsField.set(null, newMethods)
    }
}
