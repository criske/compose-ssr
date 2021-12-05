@file:Suppress("unused")

package cpf.crskdev.compose.ssr

import android.app.Application
import androidx.compose.ui.unit.ExperimentalUnitApi
import cpf.crskdev.compose.samples.clicker.ClickerSetup

/**
 * Created by Cristian Pela on 03.12.2021.
 */
class MyApp : Application() {

    @ExperimentalUnitApi
    override fun onCreate() {
        super.onCreate()
        ClickerSetup.install(this)
    }
}