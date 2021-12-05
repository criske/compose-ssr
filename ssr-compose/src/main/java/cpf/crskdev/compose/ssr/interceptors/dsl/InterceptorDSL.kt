package cpf.crskdev.compose.ssr.interceptors.dsl

import android.net.Uri
import androidx.compose.runtime.Composable
import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.interceptors.core.Interceptor
import cpf.crskdev.compose.ssr.interceptors.core.Request
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Cristian Pela on 03.12.2021.
 */
internal class InterceptorDSL(private val dslDescriptor: DSLDescriptor) : Interceptor {

    override suspend fun interceptFromClient(
        request: Request,
        sendBackToClient: suspend (Response) -> Unit,
        forward: suspend (Request) -> Unit
    ) {
        dslDescriptor.fromClient?.applyScope(request, sendBackToClient, forward)
    }

    override fun acceptFromClient(uri: Uri): Boolean = this
        .dslDescriptor
        .fromClient
        ?.matching
        ?.matches(uri) == true

    override suspend fun interceptFromServer(response: Response, forward: suspend (Response) -> Unit) {
        dslDescriptor.fromServer?.applyScope(response, forward)
    }

    override fun acceptFromServer(uri: Uri): Boolean = this
        .dslDescriptor
        .fromServer
        ?.matching
        ?.matches(uri) == true

    override suspend fun ComponentContext.onInteract(interactor: Interactor, coroutineScope: CoroutineScope) {
        dslDescriptor.onInteract?.applyScope(this, interactor, coroutineScope)
    }

    override fun acceptInteractScreen(id: String): Boolean = id == this.dslDescriptor.onInteract?.screenId

    override fun ComponentContext.onCompose(): @Composable () -> Unit =
        dslDescriptor.onCompose?.applyScope(this) ?: {}

    override fun acceptComposeScreen(id: String): Boolean = id == this.dslDescriptor.onCompose?.screenId
}

fun interceptor(block: InterceptorScope.() -> Unit): Interceptor {
    val callbacksDescriptor = DSLDescriptor()
    InterceptorScopeImpl(callbacksDescriptor).apply(block)
    return InterceptorDSL(callbacksDescriptor)
}