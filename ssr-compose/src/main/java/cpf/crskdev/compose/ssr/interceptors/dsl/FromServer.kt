package cpf.crskdev.compose.ssr.interceptors.dsl

import cpf.crskdev.compose.ssr.backend.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

class FromServer(
    internal val matching: UriMatching,
    private val scopeBlock: suspend FromServer.Scope.(CoroutineScope) -> Unit
) {

    interface Scope {
        val response: Response
        val forward: suspend (Response) -> Unit
    }

    suspend fun applyScope(response: Response, forward: suspend (Response) -> Unit) {
        val scope = object : Scope {
            override val response: Response
                get() = response
            override val forward: suspend (Response) -> Unit
                get() = forward
        }
        coroutineScope {
            scope.scopeBlock(this)
        }
    }
}
