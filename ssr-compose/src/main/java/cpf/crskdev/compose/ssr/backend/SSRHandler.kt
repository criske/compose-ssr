package cpf.crskdev.compose.ssr.backend

import android.net.Uri

/**
 * Created by Cristian Pela on 20.11.2021.
 */
interface SSRHandler {

    fun accept(uri: Uri): Boolean

    fun handle(uri: Uri, jsonBody: String): Rendered
}