package cpf.crskdev.compose.ssr.interceptors

import android.content.UriMatcher
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import cpf.crskdev.compose.ssr.Component
import cpf.crskdev.compose.ssr.ComponentContext
import cpf.crskdev.compose.ssr.Interactor
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.edit
import cpf.crskdev.compose.ssr.interceptors.core.Interceptor
import cpf.crskdev.compose.ssr.interceptors.core.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Cristian Pela on 21.11.2021.
 */
class DashboardScreenInterceptor(private val gson: Gson) : Interceptor {

    companion object {
        private val URI = Uri.parse("https://ssr/dashboard")
        private const val URI_MATCH = 1
    }

    private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI("ssr", "/dashboard", URI_MATCH)
    }

    //normally this should be from local storage/db
    private val cacheList = CopyOnWriteArrayList<LinkedTreeMap<String, Any>>()

    override suspend fun interceptFromClient(
        request: Request,
        sendBackToClient: suspend (Response) -> Unit,
        forward: suspend (Request) -> Unit
    ) = coroutineScope {
        if (request.page != null) {
            val edited = withContext(Dispatchers.IO) {
                gson.edit(request.currentScreen) { map ->
                    val comp = map.obj("content/children[1]")
                    val arr = comp.arr("children") as ArrayList
                    val start = request.page.itemsSize
                    val end = (start + request.page.size).coerceAtMost(cacheList.size)
                    arr.addAll(cacheList.subList(start, end))
                    comp["pageNo"] = request.page.number + 1
                    comp["pageSize"] = request.page.size
                    comp["pageTotal"] = request.page.total
                }
            }
            sendBackToClient(Response(request.uri, edited))
        } else {
            forward(request)
        }
    }

    override suspend fun interceptFromServer(
        response: Response,
        forward: suspend (Response) -> Unit
    ) = coroutineScope {
        val edited = withContext(Dispatchers.IO) {
            cacheList.clear()
            gson.edit(response.data) { map ->
                val comp = map.obj("content/children[1]")
                val arr = comp.arr("children") as ArrayList
                cacheList.addAll(arr)
                arr.clear()
                arr.addAll(cacheList.subList(0, 35.coerceAtMost(cacheList.size)))
                comp["pageNo"] = 1
                comp["pageSize"] = 35
                comp["pageTotal"] = cacheList.size
            }
        }
        forward(response.copy(data = edited))
    }

    override suspend fun ComponentContext.onCompose(interactor: Interactor, coroutineScope: CoroutineScope) {
        id<Component.Group.PagedList>("list") {
            onPageEndReached = { page ->
                interactor.debugToast("Reached end of page ${page.number}")
                interactor.request(URI, page = page)
            }
        }
    }

    override fun acceptFromServer(uri: Uri): Boolean = matcher.match(uri) == URI_MATCH

    override fun acceptFromClient(uri: Uri): Boolean = matcher.match(uri) == URI_MATCH

    override fun acceptScreen(id: String): Boolean = id == "dashboardScreen"
}