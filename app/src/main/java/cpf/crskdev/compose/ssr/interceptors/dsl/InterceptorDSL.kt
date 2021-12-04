package cpf.crskdev.compose.ssr.interceptors.dsl

import android.net.Uri
import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.interceptors.core.Interceptor
import cpf.crskdev.compose.ssr.interceptors.core.Request
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Cristian Pela on 03.12.2021.
 */
internal class InterceptorDSL(private val callbacksDescriptor: CallbacksDescriptor) : Interceptor {

    override suspend fun interceptFromClient(
        request: Request,
        sendBackToClient: suspend (Response) -> Unit,
        forward: suspend (Request) -> Unit
    ) {
        callbacksDescriptor.fromClient?.applyScope(request, sendBackToClient, forward)
    }

    override fun acceptFromClient(uri: Uri): Boolean = this
        .callbacksDescriptor
        .fromClient
        ?.matching
        ?.matches(uri) == true

    override suspend fun interceptFromServer(response: Response, forward: suspend (Response) -> Unit) {
        callbacksDescriptor.fromServer?.applyScope(response, forward)
    }

    override fun acceptFromServer(uri: Uri): Boolean = this
        .callbacksDescriptor
        .fromServer
        ?.matching
        ?.matches(uri) == true

    override suspend fun ComponentContext.onCompose(interactor: Interactor, coroutineScope: CoroutineScope) {
        callbacksDescriptor.onCompose?.applyScope(this, interactor, coroutineScope)
    }

    override fun acceptScreen(id: String): Boolean = id == this.callbacksDescriptor.onCompose?.screenId
}

fun interceptor(block: InterceptorScope.() -> Unit): Interceptor {
    val callbacksDescriptor = CallbacksDescriptor()
    InterceptorScopeImpl(callbacksDescriptor).apply(block)
    return InterceptorDSL(callbacksDescriptor)
}