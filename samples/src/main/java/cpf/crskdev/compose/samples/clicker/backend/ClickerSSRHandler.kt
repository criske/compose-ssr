package cpf.crskdev.compose.samples.clicker.backend

import android.net.Uri
import cpf.crskdev.compose.ssr.backend.Rendered
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.backend.SSRHandler
import cpf.crskdev.compose.ssr.interceptors.dsl.matching

/**
 * Created by Cristian Pela on 05.12.2021.
 */
class ClickerSSRHandler : SSRHandler {

    private val matcher = matching("clicker.net", "/", "/count/#")

    companion object {
        private val TEMPLATE: (Int) -> String = { count ->
            """
                {
                    "id" : "count-screen",
                    "type" : "screen",
                    "content": {
                        "id" : "content",
                        "type": "container",
                        "alignment" : {
                            "main-axis" : "center",
                            "cross-axis" : "center"
                        },
                        "children" : [
                         {
                            "id": "count-txt",
                            "type": "text",
                            "text": "$count",
                            "style": {
                                "font-weight" : 600,
                                "font-size": 64
                            }
                        },
              
                            {
                                "id" : "count-btn",
                                "type" : "button",
                                "text" : "Click me!"
                            }
                        ]
                    }
                }
            """.trimIndent()
        }
    }

    override fun accept(uri: Uri): Boolean = matcher.matches(uri)

    override fun handle(uri: Uri, jsonBody: String): Rendered {
        val query = uri.pathSegments.run {
            val countIndex = indexOf("count")
            if (countIndex != -1 && countIndex < lastIndex) {
                this[countIndex + 1].toInt() + 1
            } else {
                0
            }
        }
        return Rendered(Response(uri, TEMPLATE(query)))
    }
}