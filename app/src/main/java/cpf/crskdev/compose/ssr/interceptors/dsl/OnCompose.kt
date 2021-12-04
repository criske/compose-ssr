package cpf.crskdev.compose.ssr.interceptors.dsl

import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import kotlinx.coroutines.CoroutineScope

class OnCompose(val screenId: String, private val block: suspend ComponentContext.(Interactor, CoroutineScope) -> Unit) {

    suspend fun applyScope(componentContext: ComponentContext, interactor: Interactor, coroutineScope: CoroutineScope) {
        componentContext.block(interactor, coroutineScope)
    }
}
