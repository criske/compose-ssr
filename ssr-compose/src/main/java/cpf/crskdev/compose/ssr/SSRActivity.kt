@file:Suppress("FunctionName")

package cpf.crskdev.compose.ssr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cpf.crskdev.compose.ssr.interceptors.core.InterceptorManager
import cpf.crskdev.compose.ssr.interceptors.core.InterceptorManagerImpl

class SSRActivity : ComponentActivity() {

    private lateinit var installerParts: SSRInstaller.Parts

    private val interceptorManager by lazy {
        InterceptorManagerImpl(
            installerParts.service,
            installerParts.interceptors.toList(),
        )
    }

    private val viewModel by viewModels<ComponentViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ComponentViewModel(installerParts.entryPoint, interceptorManager, application) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val componentState = viewModel.state.collectAsState(initial = Component.None)
            App(componentState.value, interceptorManager, viewModel)
        }
    }

    override fun onBackPressed() {
        if (viewModel.onBackPressed()) {
            super.onBackPressed()
        }
    }

    internal fun install(parts: SSRInstaller.Parts) {
        this.installerParts = parts
    }
}

@Composable
fun App(component: Component, interceptorManager: InterceptorManager, interactor: Interactor) {
    val (screen, _) = remember(component) {
        mutableStateOf(component)
    }

    val coroutineScope = rememberCoroutineScope()

    Screen(screen) {
        if (component is Component.Group.Screen) {
            interceptorManager.apply {
                onCompose(component.id, interactor, coroutineScope)
            }
        }
    }
}