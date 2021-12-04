package cpf.crskdev.compose.ssr.interceptors.core

import android.net.Uri
import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.backend.SSRService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InterceptorManagerImpl(
    private val ssrService: SSRService,
    private val interceptors: List<Interceptor>
) : InterceptorManager {

    private val responseFlow = MutableStateFlow(Response(Uri.EMPTY, "{}"))

    private val sendBackToClient: suspend (Response) -> Unit = {
        responseFlow.emit(it)
    }

    private val forwardToServer: suspend (Request) -> Unit = { request ->
        val response = this.ssrService.request(request.uri, request.jsonBody)
        val fromServerInterceptor = interceptors.firstOrNull { it.acceptFromServer(response.uri) }
        if (fromServerInterceptor != null) {
            fromServerInterceptor.interceptFromServer(response, sendBackToClient)
        } else {
            sendBackToClient(response)
        }
    }

    override suspend fun request(request: Request) {
        val fromClientInterceptor = this.interceptors.firstOrNull { it.acceptFromClient(request.uri) }
        if (fromClientInterceptor != null) {
            fromClientInterceptor.interceptFromClient(request, sendBackToClient, forwardToServer)
        } else {
            val response = this.ssrService.request(request.uri, request.jsonBody)
            sendBackToClient(response)
        }
    }

    override fun responseFlow(): StateFlow<Response> = responseFlow

    override suspend fun ComponentContext.onCompose(screenId: String, interactor: Interactor, coroutineScope: CoroutineScope) {
        interceptors
            .firstOrNull { it.acceptScreen(screenId) }
            ?.apply { onCompose(interactor, coroutineScope) }
    }

}