package dev.umerov.project.module

import android.content.Context

object ProjectNative {
    @get:Synchronized
    @Volatile
    var isNativeLoaded = false
        private set

    @Volatile
    private var mAppContext: Context? = null

    @Volatile
    private var receiverDensity: OnGetDensity? = null

    @Synchronized
    fun loadNativeLibrary(exceptionControl: NativeOnException) {
        if (isNativeLoaded) {
            return
        }
        try {
            System.loadLibrary("native_jni")
            isNativeLoaded = true
        } catch (e: Error) {
            exceptionControl.onException(e)
        }
    }

    @Synchronized
    fun updateAppContext(context: Context) {
        mAppContext = context
    }

    @Synchronized
    fun updateDensity(rDensity: OnGetDensity) {
        receiverDensity = rDensity
    }

    @get:Synchronized
    val density: Float
        get() = receiverDensity?.get() ?: 0f

    @get:Synchronized
    val appContext: Context
        get() = mAppContext!!

    interface NativeOnException {
        fun onException(e: Error)
    }

    interface OnGetDensity {
        fun get(): Float
    }
}
