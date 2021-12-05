package cpf.crskdev.compose.ssr.interceptors.dsl

import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import kotlinx.coroutines.CoroutineScope

class OnInteract(
    internal val screenId: String,
    private val callback: suspend ComponentContext.(Interactor, CoroutineScope) -> Unit
) {

    suspend fun applyScope(componentContext: ComponentContext, interactor: Interactor, coroutineScope: CoroutineScope) {
        componentContext.callback(interactor, coroutineScope)
    }
}
