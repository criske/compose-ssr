package cpf.crskdev.compose.ssr.interceptors.dsl

import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor

class OnCompose(val screenId: String, private val block: ComponentContext.(Interactor) -> Unit) {

    fun applyScope(componentContext: ComponentContext, interactor: Interactor) {
        componentContext.block(interactor)
    }
}
