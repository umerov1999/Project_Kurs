package dev.umerov.project

import android.content.Context
import dev.umerov.project.App.Companion.instance
import dev.umerov.project.db.impl.AppStorages
import dev.umerov.project.db.interfaces.IStorages
import dev.umerov.project.settings.ISettings
import dev.umerov.project.settings.SettingsImpl
import dev.umerov.project.util.coroutines.CoroutinesUtils

object Includes {
    val settings: ISettings by lazy {
        SettingsImpl(instance)
    }

    val stores: IStorages by lazy {
        AppStorages(instance)
    }

    fun provideApplicationContext(): Context {
        return instance
    }

    val needReadRegister = CoroutinesUtils.createPublishSubject<Boolean>()
}
