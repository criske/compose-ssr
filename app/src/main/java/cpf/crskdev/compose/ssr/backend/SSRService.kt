package cpf.crskdev.compose.ssr.backend

import android.net.Uri
import com.google.gson.Gson
import cpf.crskdev.compose.ssr.backend.handlers.DashboardSSRHandler
import cpf.crskdev.compose.ssr.backend.handlers.LoginSSRHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/**
 * Created by Cristian Pela on 20.11.2021.
 */
interface SSRService {

    suspend fun request(uri: Uri, body: String = ""): Response

}

class SSRServiceDispatcher(private val handlers: List<SSRHandler>) : SSRService {

    override suspend fun request(uri: Uri, body: String): Response = coroutineScope {
       // delay(3000)
        val handler = handlers.firstOrNull { it.accept(uri) } ?: FallbackSSRHandler
        var result = handler.handle(uri, body)
        while (result is Rendered.Redirect) {
            val redirectUri = result.uri
            val redirectHandler = handlers.first { it.accept(redirectUri) }
            result = redirectHandler.handle(result.uri, result.jsonBody)
        }
        (result as Rendered.Data).value
    }

    private object FallbackSSRHandler : SSRHandler {

        override fun accept(uri: Uri): Boolean = true

        override fun handle(uri: Uri, jsonBody: String): Rendered = Rendered(
            uri.toResponse(
                """
            {
                "id": "fallbackScreen",
                "type": "screen",
                "content" : {
                    "id" : "error",
                    "type": "text",
                    "text": "No handler found for uri: $uri"
                 }
            }
        """.trimIndent()
            )
        )

    }
}