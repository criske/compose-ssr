package cpf.crskdev.compose.ssr.interceptors.core

import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import cpf.crskdev.compose.ssr.backend.Response
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by Cristian Pela on 21.11.2021.
 */
interface InterceptorManager {

    suspend fun request(request: Request)

    fun responseFlow(): StateFlow<Response>

    fun ComponentContext.onCompose(screenId: String, interactor: Interactor)
}