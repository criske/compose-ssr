package cpf.crskdev.compose.ssr.backend

import android.net.Uri

data class Response(val uri: Uri, val data: String)

fun Uri.toResponse(data: String): Response = Response(this, data)