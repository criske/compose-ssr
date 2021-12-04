@file:Suppress("unused")

package cpf.crskdev.compose.ssr

import android.app.Application
import android.net.Uri
import com.google.gson.Gson
import cpf.crskdev.compose.ssr.backend.SSRServiceDispatcher
import cpf.crskdev.compose.ssr.backend.handlers.DashboardSSRHandler
import cpf.crskdev.compose.ssr.backend.handlers.LoginSSRHandler
import cpf.crskdev.compose.ssr.interceptors.DashboardScreenInterceptor
import cpf.crskdev.compose.ssr.interceptors.SplashScreenInterceptor
import cpf.crskdev.compose.ssr.interceptors.loginScreenInterceptor

/**
 * Created by Cristian Pela on 03.12.2021.
 */
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val entryPoint = Uri.parse("https://ssr/")
        val gson = Gson()
        SSRInstaller(entryPoint)
            .interceptors(
                SplashScreenInterceptor(entryPoint),
                loginScreenInterceptor(gson),
                DashboardScreenInterceptor(gson)
            )
            .service(
                SSRServiceDispatcher(
                    listOf(
                        LoginSSRHandler(gson),
                        DashboardSSRHandler()
                    )
                )
            )
            .install(this)
    }
}