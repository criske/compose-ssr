package cpf.crskdev.compose.ssr.interceptors.dsl

import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import cpf.crskdev.compose.ssr.interceptors.core.UriMatching
import kotlinx.coroutines.CoroutineScope

interface InterceptorScope {

    fun fromClient(match: UriMatching, block: suspend FromClient.Scope.(CoroutineScope) -> Unit)

    fun fromServer(match: UriMatching, block: suspend FromServer.Scope.(CoroutineScope) -> Unit)

    fun onCompose(screenId: String, block: suspend ComponentContext.(Interactor, CoroutineScope) -> Unit)

}

internal class InterceptorScopeImpl(private val callbacksDescriptor: CallbacksDescriptor) : InterceptorScope {

    override fun fromClient(
        match: UriMatching,
        block: suspend FromClient.Scope.(CoroutineScope) -> Unit
    ) {
        callbacksDescriptor.fromClient = FromClient(match, block)
    }

    override fun fromServer(
        match: UriMatching,
        block: suspend FromServer.Scope.(CoroutineScope) -> Unit
    ) {
        callbacksDescriptor.fromServer = FromServer(match, block)
    }

    override fun onCompose(screenId: String, block: suspend ComponentContext.(Interactor, CoroutineScope) -> Unit) {
        callbacksDescriptor.onCompose = OnCompose(screenId, block)
    }
}