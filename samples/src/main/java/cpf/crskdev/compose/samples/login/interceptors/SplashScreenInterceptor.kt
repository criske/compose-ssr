package cpf.crskdev.compose.samples.login.interceptors

import android.net.Uri
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.interceptors.core.Interceptor
import cpf.crskdev.compose.ssr.interceptors.core.Request
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Created by Cristian Pela on 21.11.2021.
 */
class SplashScreenInterceptor(
    private val entryPoint: Uri,
    private val delayMs: Long = 500,
) : Interceptor {

    companion object {
        private const val TEMPLATE = """
            {
                "id": "splash",
                "type": "screen",
                "history": false,
                "content": {
                    "id": "splash-text",
                    "type": "text",
                    "text": "Loading..."
                }
            }
        """
    }

    override suspend fun interceptFromClient(
        request: Request,
        sendBackToClient: suspend (Response) -> Unit,
        forward: suspend (Request) -> Unit
    ) = coroutineScope {
        val requestJob = launch {
            forward(request)
        }
        val splashJob = launch {
            delay(delayMs) // delay a bit to give a chance for requestJob to complete, otherwise show the splash
            if (isActive)
                sendBackToClient(Response(entryPoint, TEMPLATE))
        }
        requestJob.invokeOnCompletion {
            splashJob.cancel()
        }
        Unit
    }

    override fun acceptFromClient(uri: Uri): Boolean = this.entryPoint == uri
}