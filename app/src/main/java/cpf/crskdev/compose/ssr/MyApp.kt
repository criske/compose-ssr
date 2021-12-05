@file:Suppress("unused")

package cpf.crskdev.compose.ssr

import android.app.Application
import androidx.compose.ui.unit.ExperimentalUnitApi
import cpf.crskdev.compose.samples.clicker.ClickerSetup
import cpf.crskdev.compose.samples.login.LoginSetup

/**
 * Created by Cristian Pela on 03.12.2021.
 */
@ExperimentalUnitApi
class MyApp : Application() {


    override fun onCreate() {
        super.onCreate()
       // ClickerSetup.install(this)
        LoginSetup.install(this)
    }
}