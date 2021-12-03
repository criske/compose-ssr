package cpf.crskdev.compose.ssr.backend.handlers

import android.content.UriMatcher
import android.net.Uri
import cpf.crskdev.compose.ssr.backend.Rendered
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.backend.SSRHandler

/**
 * Created by Cristian Pela on 20.11.2021.
 */
class DashboardSSRHandler : SSRHandler {

    companion object {
        private const val DASHBOARD_SCREEN = 1

        private val messages = (1..100).joinToString(",") {
            """
               {
                 "id": "msg$it",
                 "type": "text",
                 "text": "This is a message having number $it",
                 "padding": "0,8,0,8",
                 "border": "1,#4f4747",
                 "width": "fill",
                 "height": "64",
                 "background": "#C1C1C2"
               }
            """.trimIndent()
        }

        private val TEMPLATE: (String) -> String = { username ->
            """ 
                    {
                        "id": "dashboardScreen",
                        "type": "screen",
                        "history": true,
                        "path": "/dashboard",
                        "content": {
                            "id": "container",
                            "type": "container",
                            "children": [
                                {
                                    "id": "hello",
                                    "type": "text",
                                    "text": "Welcome back ${username.trim()}"
                                },
                                {
                                    "id":"list",
                                    "type":"list",
                                    "children": [
                                        $messages
                                    ]
                                }
                            ]
                         }
                    }
                    """
        }
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI("ssr", "/dashboard", DASHBOARD_SCREEN)
    }

    override fun accept(uri: Uri): Boolean = uriMatcher.match(uri) == DASHBOARD_SCREEN

    override fun handle(uri: Uri, jsonBody: String): Rendered {
        val username = uri.getQueryParameter("username") ?: ""
        return Rendered(Response(Uri.parse("https://ssr/dashboard"), TEMPLATE(username)))
    }
}