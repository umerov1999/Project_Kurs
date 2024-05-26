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

        var volumeValue: Float

        @get:Lang
        val language: Int

        @AppCompatDelegate.NightMode
        val nightMode: Int
        val isDeveloper_mode: Boolean
        val isEnable_dirs_files_count: Boolean

        val slidrSettings: SlidrSettings
        fun setSlidrSettings(settings: SlidrSettings)
        val maxBitmapResolution: Int
        val maxThumbResolution: Int
        val rendering_mode: Int
        val isInstant_photo_display: Boolean
        val photoExt: Set<String>
        val audioExt: Set<String>

        val viewpager_page_transform: Int
        val isLimitImage_cache: Int
        val picassoDispatcher: Int

        var snakeScore: Int
        var snakeVolumeValue: Int
        var snakeRefreshMS: Int
        var enableAccelerometer: Boolean
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
