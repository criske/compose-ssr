package cpf.crskdev.compose.ssr.interceptors.dsl

import android.net.Uri
import cpf.crskdev.compose.ssr.backend.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class FromServer(
    internal val uri: Uri,
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
