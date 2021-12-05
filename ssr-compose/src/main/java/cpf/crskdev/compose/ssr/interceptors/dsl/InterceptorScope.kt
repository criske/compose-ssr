package cpf.crskdev.compose.ssr.interceptors.dsl

import androidx.compose.runtime.Composable
import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import kotlinx.coroutines.CoroutineScope

interface InterceptorScope {

    fun fromClient(match: UriMatching, block: suspend FromClient.Scope.(CoroutineScope) -> Unit)

    fun fromServer(match: UriMatching, block: suspend FromServer.Scope.(CoroutineScope) -> Unit)

    fun onInteract(screenId: String, block: suspend ComponentContext.(Interactor, CoroutineScope) -> Unit)

    fun onCompose(screenId: String, block: ComponentContext.() -> @Composable ()-> Unit)
}

internal class InterceptorScopeImpl(private val dslDescriptor: DSLDescriptor) : InterceptorScope {

    override fun fromClient(
        match: UriMatching,
        block: suspend FromClient.Scope.(CoroutineScope) -> Unit
    ) {
        dslDescriptor.fromClient = FromClient(match, block)
    }

    override fun fromServer(
        match: UriMatching,
        block: suspend FromServer.Scope.(CoroutineScope) -> Unit
    ) {
        dslDescriptor.fromServer = FromServer(match, block)
    }

    override fun onInteract(screenId: String, block: suspend ComponentContext.(Interactor, CoroutineScope) -> Unit) {
        dslDescriptor.onInteract = OnInteract(screenId, block)
    }

    override fun onCompose(screenId: String, block: ComponentContext.() -> @Composable () -> Unit) {
        dslDescriptor.onCompose = OnCompose(screenId, block)
    }
}