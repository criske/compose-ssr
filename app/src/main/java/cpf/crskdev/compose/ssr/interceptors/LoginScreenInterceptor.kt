package cpf.crskdev.compose.ssr.interceptors

import android.content.UriMatcher
import android.net.Uri
import com.google.gson.Gson
import cpf.crskdev.compose.ssr.*
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.interceptors.core.Interceptor
import cpf.crskdev.compose.ssr.interceptors.core.Request

/**
 * Created by Cristian Pela on 21.11.2021.
 */
class LoginScreenInterceptor(private val gson: Gson) : Interceptor {

    companion object {
        private const val LOGIN_ACTION = 1
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI("ssr", "/login", LOGIN_ACTION)
    }

    override suspend fun interceptFromClient(
        request: Request,
        sendBackToClient: suspend (Response) -> Unit,
        forward: suspend (Request) -> Unit
    ) {
        val (username, password) = gson.read(request.jsonBody) { map ->
            map.value<String>("username").trim() to map.value<String>("password").trim()
        }
        if (username.isBlank() || password.isBlank()) {
            sendBackToClient(
                Response(
                    request.uri,
                    gson.edit(request.currentScreen) { map ->
                        val form = map.arr("content/children")
                        form[1]["text"] = username // keep the username filled
                        form[4]["text"] = "Username and password must be filled!" // error message
                    }
                )
            )
        } else {
            sendBackToClient(
                Response(
                    request.uri,
                    gson.edit(request.currentScreen) { map ->
                        val form = map.arr("content/children")
                        form[1]["text"] = username // keep the username filled
                        form[2]["text"] = password // keep password filled
                        form[3]["disabled"] = true // disable login button while request is processed by server
                        form[4]["text"] = "" // clear the errors
                    }
                )
            )
            forward(request)
        }
    }

    override fun ComponentContext.onCompose(interactor: Interactor) {
        id<Component.Button>("loginBtn") {
            onClick = {
                val username = id<Component.TextField>("inputUserName")!!.text
                val password = id<Component.TextField>("inputPassword")!!.text
                interactor.request(
                    Uri.parse("https://ssr/login"),
                    """
                    {
                        "username" : "$username",
                        "password" : "$password"
                    }
                """.trimIndent()
                )
            }
        }
    }

    override fun acceptFromClient(uri: Uri): Boolean = uriMatcher.match(uri) == LOGIN_ACTION

    override fun acceptScreen(id: String): Boolean  = id == "loginScreen"
}