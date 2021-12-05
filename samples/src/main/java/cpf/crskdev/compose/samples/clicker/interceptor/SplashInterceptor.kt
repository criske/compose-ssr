package cpf.crskdev.compose.samples.clicker.interceptor

import android.net.Uri
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.interceptors.dsl.exactMatching
import cpf.crskdev.compose.ssr.interceptors.dsl.interceptor
import cpf.crskdev.compose.ssr.interceptors.dsl.sendBackToClientOnSlowRequest

/**
 * Created by Cristian Pela on 05.12.2021.
 */
val splashScreenInterceptor = interceptor {

    fromClient(Uri.parse("https://clicker.net").exactMatching()) {
        sendBackToClientOnSlowRequest {
            Response(request.uri, """
                {
                    "id" : "splash",
                    "type" : "screen",
                    "width": "fill",
                    "height": "fill",
                    "alignment" : {
                        "main-axis" : "center",
                        "cross-axis" : "center"
                    },
                    "content" : {
                        "id" : "txt", 
                        "type": "text",
                        "text" : "Loading...",
                         "style": {
                             "font-weight" : 600,
                             "font-size": 32
                         }   
                    }
                }
            """.trimIndent())
        }
    }
}