package cpf.crskdev.compose.ssr.interceptors

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.gson.Gson
import cpf.crskdev.compose.ssr.Component
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.edit
import cpf.crskdev.compose.ssr.interceptors.core.Interceptor
import cpf.crskdev.compose.ssr.interceptors.dsl.exactMatching
import cpf.crskdev.compose.ssr.interceptors.dsl.interceptor
import cpf.crskdev.compose.ssr.read
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * Created by Cristian Pela on 21.11.2021.
 */

val loginScreenInterceptor: (Gson) -> Interceptor = { gson ->

    interceptor {

        fromClient(Uri.parse("https://ssr/login").exactMatching()) {
            val (username, password) = gson.read(request.jsonBody) { map ->
                map.value<String>("username").trim() to map.value<String>("password").trim()
            }
            if (username.isBlank() || password.isBlank()) {
                sendBackToClient(
                    Response(
                        request.uri,
                        gson.edit(request.currentScreen) { map ->
                            val form = map.arr("content/children[0]/children")
                            form[1]["text"] = username // keep the username filled
                            form[3]["text"] = "Username and password must be filled!" // error message
                        }
                    )
                )
            } else {
                sendBackToClient(
                    Response(
                        request.uri,
                        gson.edit(request.currentScreen) { map ->
                            val form = map.arr("content/children[0]/children")
                            form[1]["text"] = username // keep the username filled
                            form[2]["text"] = password // keep password filled
                            form[3]["text"] = "" // clear the errors
                            form[4]["disabled"] = true // disable login button while request is processed by server
                        }
                    )
                )
                forward(request)
            }
        }

        onInteract("loginScreen") { interactor, _ ->

            id<Component.Button>("loginBtn")
                ?.clickFlow<InputCredentials>(300) { emit ->
                    val username = id<Component.TextField>("inputUserName")!!.text
                    val password = id<Component.TextField>("inputPassword")!!.text
                    emit(username to password)
                }
                ?.collect {
                    val (username, password) = it
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

//        onCompose("loginScreen")  {
//            @Composable {
//                Box(modifier = Modifier.fillMaxSize()) {
//                    id<Component.Text>("title") {
//                        Text(text = text, modifier = Modifier.align(Alignment.Center))
//                    }
//                }
//            }
//        }
    }
}

typealias InputCredentials = Pair<String, String>

@FlowPreview
@ExperimentalCoroutinesApi
private fun <T> Component.Button.clickFlow(
    debounceMillis: Long = 0,
    onClick: (emit: (T) -> Unit) -> Unit): Flow<T> {
    val clickable = this
    return channelFlow {
        val emit: (T) -> Unit = { trySend(it) }
        clickable.onClick = { onClick(emit) }
        awaitClose {
            clickable.onClick = {}
        }
    }.distinctUntilChanged()
        .debounce(debounceMillis)
}