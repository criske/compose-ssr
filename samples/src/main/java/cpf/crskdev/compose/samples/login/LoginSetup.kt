package cpf.crskdev.compose.samples.login

import android.net.Uri
import com.google.gson.Gson
import cpf.crskdev.compose.samples.login.backend.DashboardSSRHandler
import cpf.crskdev.compose.samples.login.backend.LoginSSRHandler
import cpf.crskdev.compose.samples.login.interceptors.DashboardScreenInterceptor
import cpf.crskdev.compose.samples.login.interceptors.SplashScreenInterceptor
import cpf.crskdev.compose.samples.login.interceptors.loginScreenInterceptor
import cpf.crskdev.compose.ssr.SSRInstaller
import cpf.crskdev.compose.ssr.backend.FakeSSRService

/**
 * Created by Cristian Pela on 05.12.2021.
 */

private val entryPoint = Uri.parse("https://ssr/")
private val gson = Gson()

val LoginSetup = SSRInstaller(entryPoint)
    .interceptors(
        SplashScreenInterceptor(entryPoint),
        loginScreenInterceptor(gson),
        DashboardScreenInterceptor(gson)
    )
    .service(
        FakeSSRService(
            listOf(
                LoginSSRHandler(gson),
                DashboardSSRHandler()
            )
        )
    )
