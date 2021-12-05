package cpf.crskdev.compose.samples.clicker.interceptor

import com.google.gson.Gson
import cpf.crskdev.compose.ssr.Component
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.edit
import cpf.crskdev.compose.ssr.interceptors.core.Interceptor
import cpf.crskdev.compose.ssr.interceptors.dsl.interceptor
import cpf.crskdev.compose.ssr.interceptors.dsl.matching
import cpf.crskdev.compose.ssr.interceptors.dsl.sendBackToClientOnSlowRequest
import cpf.crskdev.compose.ssr.request

/**
 * Created by Cristian Pela on 05.12.2021.
 */

val clickerInterceptor: (Gson) -> Interceptor = { gson ->

    interceptor {

        val matchClickRequest = matching("clicker.net", "/count/#")

        fromClient(matchClickRequest) {
            sendBackToClientOnSlowRequest {
                val disabledClickBtn = gson.edit(this.request.currentScreen) { json ->
                    val clickBtn = json.obj("content/children[1]")
                    clickBtn["disabled"] = true
                }
                Response(this.request.uri, disabledClickBtn)
            }
        }

        onInteract("count-screen") { interactor, _ ->
            val count = id<Component.Text>("count-txt")?.text?.toInt() ?: 0
            if (count == 3) {
                interactor.debugToast("Hooray you've count to 3. Good bye for now!")
                interactor.closeApp()
            }
            id<Component.Button>("count-btn") {
                onClick = {
                    interactor.request("https://clicker.net/count/$count")
                }
            }
        }

    }
}