package cpf.crskdev.compose.ssr.interceptors.dsl

import android.net.Uri
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.interceptors.core.Request
import cpf.crskdev.compose.ssr.interceptors.core.UriMatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

/**
 * Created by Cristian Pela on 03.12.2021.
 */
class FromClient(
    internal val matching: UriMatching,
    private val blockScope: suspend FromClient.Scope.(CoroutineScope) -> Unit) {

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
            scope.blockScope(this)
        }
    }
}