package cpf.crskdev.compose.ssr.interceptors.core

import android.net.Uri
import androidx.compose.runtime.Composable
import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import cpf.crskdev.compose.ssr.backend.Response
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Cristian Pela on 21.11.2021.
 */
interface Interceptor {

    suspend fun interceptFromClient(
        request: Request,
        sendBackToClient: suspend (Response) -> Unit,
        forward: suspend (Request) -> Unit) {
        forward(request)
    }

    suspend fun interceptFromServer(
        response: Response,
        forward: suspend (Response) -> Unit
    ) {
        forward(response)
    }

    suspend fun ComponentContext.onInteract(interactor: Interactor, coroutineScope: CoroutineScope) {
    }

    fun ComponentContext.onCompose(): @Composable () -> Unit = {}

    fun acceptFromServer(uri: Uri): Boolean = false

    fun acceptFromClient(uri: Uri): Boolean = false

    fun acceptInteractScreen(id: String): Boolean = false

    fun acceptComposeScreen(id: String): Boolean = false

}