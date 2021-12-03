package cpf.crskdev.compose.ssr

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Bundle
import cpf.crskdev.compose.ssr.backend.Response
import cpf.crskdev.compose.ssr.backend.SSRService
import cpf.crskdev.compose.ssr.interceptors.core.Interceptor

/**
 * Created by Cristian Pela on 03.12.2021.
 */
class SSRInstaller(private val entryPoint: Uri) {

    private val interceptors: MutableSet<Interceptor> = mutableSetOf()

    private var service: SSRService = NoSSRService

    fun interceptors(vararg interceptors: Interceptor): SSRInstaller {
        this.interceptors.addAll(interceptors)
        return this
    }

    fun service(service: SSRService): SSRInstaller {
        this.service = service
        return this
    }

    fun install(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is SSRActivity) {
                    activity.install(Parts(entryPoint, interceptors, service))
                }
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }

        })
    }

    internal class Parts(
        val entryPoint: Uri,
        val interceptors: Set<Interceptor>,
        val service: SSRService
    )

    private object NoSSRService: SSRService {
        override suspend fun request(uri: Uri, body: String): Response {
            throw IllegalStateException("An SSRService must be installed!")
        }
    }

}