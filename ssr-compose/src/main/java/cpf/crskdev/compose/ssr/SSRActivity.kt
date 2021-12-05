@file:Suppress("FunctionName")

package cpf.crskdev.compose.ssr

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cpf.crskdev.compose.ssr.interceptors.core.InterceptorManager
import cpf.crskdev.compose.ssr.interceptors.core.InterceptorManagerImpl

@ExperimentalUnitApi
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

    private val appCloseHandle: () -> Unit = { finish() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.appCloseHandle = this.appCloseHandle
        setContent {
            val componentState = viewModel.state.collectAsState(initial = Component.None)
            val component = componentState.value
            if (component is Component.Group.Screen) {
                LaunchedEffect(component.theme.colors.primaryVariant) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = component.theme.colors.primaryVariant.nativeColorInt
                }
            }

            MaterialTheme(colors = if (component is Component.Group.Screen) component.theme.colors else MaterialTheme.colors) {
                App(component, interceptorManager, viewModel)
            }
        }
    }

    override fun onDestroy() {
        //cleanup to prevent leaks
        viewModel.appCloseHandle = null
        super.onDestroy()
    }

    private val Color.nativeColorInt: Int
        get() = toArgb().run {
            android.graphics.Color.argb(
                this.alpha,
                this.red,
                this.green,
                this.blue
            )
        }

    override fun onBackPressed() {
        if (!viewModel.onBackPressed()) {
            super.onBackPressed()
        }
    }


    internal fun install(parts: SSRInstaller.Parts) {
        this.installerParts = parts
    }
}

@Composable
private fun App(
    component: Component,
    interceptorManager: InterceptorManager,
    interactor: Interactor
) {
    if (component is Component.Group.Screen) {
        Screen(component, interceptorManager, interactor)
    }
}