package cpf.crskdev.compose.ssr.interceptors.core

import android.net.Uri

/**
 * Created by Cristian Pela on 21.11.2021.
 */
data class Request(val currentScreen: String, val uri: Uri, val jsonBody: String = "", val page: Page? = null)
