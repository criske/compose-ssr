package cpf.crskdev.compose.ssr.backend.handlers

import android.content.UriMatcher
import android.net.Uri
import com.google.gson.Gson
import cpf.crskdev.compose.ssr.backend.Rendered
import cpf.crskdev.compose.ssr.backend.SSRHandler
import cpf.crskdev.compose.ssr.backend.toResponse
import cpf.crskdev.compose.ssr.edit
import cpf.crskdev.compose.ssr.read

/**
 * Created by Cristian Pela on 20.11.2021.
 */
class LoginSSRHandler(private val gson: Gson) : SSRHandler {

    companion object {

        private const val LOGIN_SCREEN = 1

        private const val LOGIN_ACTION = 2

        private val users = mapOf(
            "admin" to "123",
            "john-doe" to "1234"
        )

        private val TEMPLATE = """
    {
        "id": "loginScreen",
        "type": "screen",
        "history": false,
        "theme": {
            "colors" : {
                "primary": "#32826C",
                "primary-variant" : "#073042",
                "background" : "#C1C1C1"
            }
        },
        "content": {
            "id": "container",
            "type": "container",
            "alignment" : {
                 "main-axis"  : "center",
                 "cross-axis" : "center"
            },
            "children" : [
                {
                    "id": "form",
                    "type": "container",
                    "width": "0.8fr",
                    "background" : "#C2F261",
                    "alignment" : {
                        "cross-axis" : "center",
                        "main-axis" : "12spacing"
                    },
                    "padding" : "0,8,0,16",
                    "children": [
                        {
                            "id": "title",
                            "type": "text",
                            "text": "Login form",
                            "style": {
                                "font-weight" : 600,
                                "font-size": 24
                            }
                        },
 			            {
                            "id": "inputUserName",
                            "type": "edit",
                            "label": "Username"
                        },
                        {
                            "id": "inputPassword",
                            "type": "edit",
                            "editType": "password",
                            "label": "Password"
                        },
                        {
                            "id": "error",
                            "type": "text",
                            "text": "",
                            "style": {
                                "color": "#EB3223",
                                "font-weight" : 600
                            } 
                        },   
                        {
                            "id": "loginBtn",
                            "type": "button",
                            "text": "Login",
                            "width": "0.85fr"
                        }                   
                    ]
                }
            ]
        }
    }
""".trimIndent()

    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI("ssr", "/login", LOGIN_SCREEN)
        addURI("ssr", "/", LOGIN_ACTION)
    }

    override fun accept(uri: Uri): Boolean {
        val match = uriMatcher.match(uri)
        return LOGIN_SCREEN.or(LOGIN_ACTION).and(match) == match
    }

    override fun handle(uri: Uri, jsonBody: String): Rendered =
        if (jsonBody.isNotBlank()) {
            val (username, password) = gson.read(jsonBody) { map ->
                map.value<String>("username").trim() to map.value<String>("password").trim()
            }
            if (username.isBlank() || password.isBlank()) {
                Rendered(uri.toResponse(error("Username and password must be filled", username)))
            } else if (!users.containsKey(username) || users[username] != password) {
                Rendered(uri.toResponse(error("Invalid credentials", username)))
            } else {
                Rendered.Redirect(Uri.parse("https://ssr/dashboard?username=$username"))
            }
        } else {
            Rendered(uri.toResponse(TEMPLATE))
        }


    private fun error(message: String, username: String): String =
        gson.edit(TEMPLATE) { json ->
            val form = json.arr("content/children[0]/children")
            form[1]["text"] = username // keep the username filled
            form[3]["text"] = message // error message
        }

}

internal typealias Credentials = Pair<String, String>