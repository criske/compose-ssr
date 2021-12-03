package cpf.crskdev.compose.ssr.interceptors.dsl

import android.net.Uri
import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.interceptors.core.Interceptor
import cpf.crskdev.compose.ssr.interceptors.core.Request

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

    override fun acceptFromClient(uri: Uri): Boolean = uri == this.callbacksDescriptor.fromClient?.uri

    override suspend fun interceptFromServer(response: Response, forward: suspend (Response) -> Unit) {
        callbacksDescriptor.fromServer?.applyScope(response, forward)
    }

    override fun acceptFromServer(uri: Uri): Boolean = uri == this.callbacksDescriptor.fromServer?.uri

    override fun ComponentContext.onCompose(interactor: Interactor) {
        callbacksDescriptor.onCompose?.applyScope(this, interactor)
    }

    override fun acceptScreen(id: String): Boolean = id == this.callbacksDescriptor.onCompose?.screenId
}

fun interceptor(block: InterceptorScope.() -> Unit): Interceptor {
    val callbacksDescriptor = CallbacksDescriptor()
    InterceptorScopeImpl(callbacksDescriptor).apply(block)
    return InterceptorDSL(callbacksDescriptor)
}