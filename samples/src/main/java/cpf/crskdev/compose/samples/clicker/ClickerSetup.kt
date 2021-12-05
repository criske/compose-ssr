package cpf.crskdev.compose.samples.clicker

import com.google.gson.Gson
import cpf.crskdev.compose.samples.clicker.backend.ClickerSSRHandler
import cpf.crskdev.compose.samples.clicker.interceptor.clickerInterceptor
import cpf.crskdev.compose.samples.clicker.interceptor.splashScreenInterceptor
import cpf.crskdev.compose.ssr.SSRInstaller
import cpf.crskdev.compose.ssr.backend.FakeSSRService
import kotlin.random.Random

/**
 * Created by Cristian Pela on 04.12.2021.
 */

val ClickerSetup = SSRInstaller("https://clicker.net")
    .service(
        FakeSSRService(
            handlers = listOf(ClickerSSRHandler()),
            latencyMillisProvider = {
                //Random.nextLong(2000)
                3000
            }
        )
    )
    .interceptors(
        splashScreenInterceptor,
        clickerInterceptor(Gson())
    )
