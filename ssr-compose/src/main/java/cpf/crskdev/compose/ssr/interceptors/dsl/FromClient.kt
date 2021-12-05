package cpf.crskdev.compose.ssr.interceptors.dsl

import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.interceptors.SplashScreenInterceptor
import cpf.crskdev.compose.ssr.interceptors.core.Request
import kotlinx.coroutines.*

/**
 * Created by Cristian Pela on 03.12.2021.
 */
class FromClient(
    internal val matching: UriMatching,
    private val callback: suspend FromClient.Scope.(CoroutineScope) -> Unit) {

    interface Scope {
        val request: Request
        val sendBackToClient: suspend (Response) -> Unit
        val forward: suspend (Request) -> Unit
    }

    internal suspend fun applyScope(
        request: Request,
        sendBackToClient: suspend (Response) -> Unit,
        forward: suspend (Request) -> Unit
    ) {
        val scope: Scope = object : Scope {
            override val request: Request
                get() = request
            override val sendBackToClient: suspend (Response) -> Unit
                get() = sendBackToClient
            override val forward: suspend (Request) -> Unit
                get() = forward

        }
        coroutineScope {
            scope.callback(this)
        }
    }
}

/**
 * Send the response block back to client if the remote server is not responding in timeoutMillis.
 *
 * Usual usage is disabling a button, showing a loading cue etc... while the server is rendering the response.
 */
suspend fun FromClient.Scope.sendBackToClientOnSlowRequest(timeoutMillis: Long = 300, block: () -> Response){
    coroutineScope {
        val requestJob = launch {
            forward(request)
        }
        val splashJob = launch {
            delay(timeoutMillis) // delay a bit to give a chance for requestJob to complete, otherwise send it back
            if (isActive)
                sendBackToClient(block())
        }
        requestJob.invokeOnCompletion {
            splashJob.cancel()
        }
        Unit
    }
}