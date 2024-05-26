package dev.umerov.project

import android.app.Application
import android.os.Handler
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.core.ImageProcessingUtil
import dev.umerov.project.activity.crash.CrashUtils
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.picasso.PicassoInstance
import dev.umerov.project.settings.Settings
import dev.umerov.project.util.Camera2ImageProcessingUtil
import dev.umerov.project.util.ErrorLocalizer
import dev.umerov.project.util.Utils
import dev.umerov.project.util.toast.CustomToast.Companion.createCustomToast
import io.reactivex.rxjava3.plugins.RxJavaPlugins

class App : Application() {
    override fun onCreate() {
        sInstanse = this

        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(Settings.get().main().nightMode)
        if (Settings.get().main().isDeveloper_mode) {
            CrashUtils.install(this)
        }

        ProjectNative.loadNativeLibrary(object : ProjectNative.NativeOnException {
            override fun onException(e: Error) {
                e.printStackTrace()
            }
        })
        ProjectNative.updateAppContext(this)
        ProjectNative.updateDensity(object : ProjectNative.OnGetDensity {
            override fun get(): Float {
                return Utils.density
            }
        })

        if (ProjectNative.isNativeLoaded) {
            ImageProcessingUtil.setProcessingUtil(Camera2ImageProcessingUtil)
        }
        PicassoInstance.init(this)
        RxJavaPlugins.setErrorHandler {
            it.printStackTrace()
            Handler(mainLooper).post {
                if (Settings.get().main().isDeveloper_mode) {
                    createCustomToast(this, null)?.showToastError(
                        ErrorLocalizer.localizeThrowable(
                            this,
                            it
                        )
                    )
                }
            }
        }
    }

    companion object {
        @Volatile
        private var sInstanse: App? = null

        val instance: App
            get() {
                checkNotNull(sInstanse) { "App instance is null!!!" }
                return sInstanse!!
            }
    }
}
