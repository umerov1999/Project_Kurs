package dev.umerov.project.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import dev.umerov.project.model.Lang
import dev.umerov.project.model.SlidrSettings
import dev.umerov.project.settings.theme.ThemeOverlay

interface ISettings {
    fun main(): IMainSettings
    fun security(): ISecuritySettings
    interface IMainSettings {
        val fontSize: Int

        @ThemeOverlay
        val themeOverlay: Int
        val mainThemeKey: String
        fun setMainTheme(key: String)
        fun switchNightMode(@AppCompatDelegate.NightMode key: Int)
        fun isDarkModeEnabled(context: Context): Boolean

        @get:Lang
        val language: Int

        @AppCompatDelegate.NightMode
        val nightMode: Int
        val isDeveloper_mode: Boolean

        val slidrSettings: SlidrSettings
        fun setSlidrSettings(settings: SlidrSettings)

        val viewpager_page_transform: Int
    }

    interface ISecuritySettings {
        fun isPinValid(values: IntArray): Boolean
        fun setPin(pin: IntArray?)
        var isEntranceByFingerprintAllowed: Boolean

        fun firePinAttemptNow()
        fun clearPinHistory()
        val pinEnterHistory: List<Long>
        val hasPinHash: Boolean
        val pinHistoryDepthValue: Int
        fun updateLastPinTime()
        var isUsePinForEntrance: Boolean
    }
}
