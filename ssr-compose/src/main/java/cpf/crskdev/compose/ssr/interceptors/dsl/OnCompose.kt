package cpf.crskdev.compose.ssr.interceptors.dsl

import androidx.compose.runtime.Composable
import cpf.crskdev.compose.ssr.ComponentContext

/**
 * Created by Cristian Pela on 05.12.2021.
 */
class OnCompose(
    internal val screenId: String,
    private val callback: ComponentContext.() -> @Composable () -> Unit
) {

    fun applyScope(componentContext: ComponentContext): @Composable () -> Unit =
        componentContext.run(callback)
}