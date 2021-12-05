package cpf.crskdev.compose.ssr

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import cpf.crskdev.compose.ssr.interceptors.core.InterceptorManager
import cpf.crskdev.compose.ssr.interceptors.core.Page
import cpf.crskdev.compose.ssr.interceptors.core.Request
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

/**
 * Created by Cristian Pela on 20.11.2021.
 */
//TODO: DON'T USE AndroidViewModel in future. Right now I'm using app context to show toasts in debug
@ExperimentalUnitApi
class ComponentViewModel(
    private val entryPoint: Uri,
    private val interceptorManager: InterceptorManager,
    application: Application,
) : AndroidViewModel(application), Interactor {

    private val responseStateFlow = interceptorManager.responseFlow()

    private val gson = GsonBuilder()
        .registerTypeAdapter(Component::class.java, ComponentDeserializer())
        .create()


    private val _state: MutableStateFlow<ComponentBackstack> = MutableStateFlow(ComponentBackstack())
    val state: Flow<Component> = _state.map { it.peek }

    private val ongoingRequestJobs = mutableListOf<Job>()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            responseStateFlow.collect {
                val screen = gson.fromJson<Component>(it.data)
                val backstack = _state.value
                val top = backstack.peek
                if (screen.id != top.id && (screen as Component.Group.Screen).history) {
                    _state.emit(backstack.push(screen))
                } else {
                    _state.emit(backstack.replaceTop(screen))
                }
            }
        }
        viewModelScope.launchOngoingJob {
            interceptorManager.request(
                Request(
                    responseStateFlow.value.data,
                    entryPoint,
                )
            )
        }
    }

    override fun onBackPressed(): Boolean {
        //canceling ongoing requests to backend first
        ongoingRequestJobs.forEach { it.cancel() }
        val willBeEmpty = _state.value.willBeEmpty
        if (!willBeEmpty)
            _state.value = _state.value.pop()
        return willBeEmpty
    }

    override fun request(uri: Uri, jsonBody: String, page: Page?) {
        viewModelScope.launchOngoingJob {
            interceptorManager.request(
                Request(
                    responseStateFlow.value.data,
                    uri,
                    jsonBody,
                    page
                )
            )
        }
    }

    override fun debugToast(message: String) {
        if (BuildConfig.DEBUG)
            Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }

    private fun CoroutineScope.launchOngoingJob(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        scope: suspend CoroutineScope.() -> Unit
    ) {
        val job = launch(dispatcher) { scope() }.apply { ongoingRequestJobs.add(this) }
        job.invokeOnCompletion {
            ongoingRequestJobs.remove(job)
        }
    }

}

interface Interactor {

    fun onBackPressed(): Boolean

    fun request(uri: Uri, jsonBody: String = "", page: Page? = null)

    fun debugToast(message: String)

}