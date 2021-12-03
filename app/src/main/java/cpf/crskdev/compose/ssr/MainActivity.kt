@file:Suppress("FunctionName")

package cpf.crskdev.compose.ssr

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import cpf.crskdev.compose.ssr.backend.SSRServiceDispatcher
import cpf.crskdev.compose.ssr.interceptors.DashboardScreenInterceptor
import cpf.crskdev.compose.ssr.interceptors.LoginScreenInterceptor
import cpf.crskdev.compose.ssr.interceptors.SplashScreenInterceptor
import cpf.crskdev.compose.ssr.interceptors.core.InterceptorManager
import cpf.crskdev.compose.ssr.interceptors.core.InterceptorManagerImpl

class MainActivity : ComponentActivity() {

    private val entryPoint = Uri.parse("https://ssr/")

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

    Screen(screen) {
        if (component is Component.Group.Screen) {
            interceptorManager.apply {
                onCompose(component.id, interactor)
            }
        }
    }
}