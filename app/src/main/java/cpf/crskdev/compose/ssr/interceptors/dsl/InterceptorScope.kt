package cpf.crskdev.compose.ssr.interceptors.dsl

import android.net.Uri
import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import kotlinx.coroutines.CoroutineScope

interface InterceptorScope {

    fun fromClient(match: Uri, block: suspend FromClient.Scope.(CoroutineScope) -> Unit)

    fun fromServer(match: Uri, block: suspend FromServer.Scope.(CoroutineScope) -> Unit)

    fun onCompose(screenId: String, block: ComponentContext.(Interactor) -> Unit)

}

internal class InterceptorScopeImpl(private val callbacksDescriptor: CallbacksDescriptor) : InterceptorScope {

    override fun fromClient(
        match: Uri,
        block: suspend FromClient.Scope.(CoroutineScope) -> Unit
    ) {
        callbacksDescriptor.fromClient = FromClient(match, block)
    }

    override fun fromServer(
        match: Uri,
        block: suspend FromServer.Scope.(CoroutineScope) -> Unit
    ) {
        callbacksDescriptor.fromServer = FromServer(match, block)
    }

    override fun onCompose(screenId: String, block: ComponentContext.(Interactor) -> Unit) {
        callbacksDescriptor.onCompose = OnCompose(screenId, block)
    }
}