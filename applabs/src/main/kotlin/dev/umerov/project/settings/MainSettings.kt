package dev.umerov.project.settings

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import de.maxr1998.modernpreferences.PreferenceScreen.Companion.getPreferences
import dev.umerov.project.Constants.forceDeveloperMode
import dev.umerov.project.kJson
import dev.umerov.project.model.Lang
import dev.umerov.project.model.SlidrSettings
import dev.umerov.project.module.FileUtils
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.settings.ISettings.IMainSettings
import dev.umerov.project.settings.theme.ThemeOverlay
import dev.umerov.project.view.pager.Transformers_Types

internal class MainSettings(context: Context) : IMainSettings {
    private val app: Context = context.applicationContext

    override val fontSize: Int
        get() = getPreferences(app).getInt("font_size_int", 0)

    @get:ThemeOverlay
    override val themeOverlay: Int
        get() = try {
            getPreferences(app).getString("theme_overlay", "0")?.trim { it <= ' ' }?.toInt()
                ?: ThemeOverlay.OFF
        } catch (e: Exception) {
            ThemeOverlay.OFF
        }

    override val mainThemeKey: String
        get() {
            val preferences = getPreferences(app)
            return preferences.getString("app_theme", "lineage")!!
        }

    override fun setMainTheme(key: String) {
        val preferences = getPreferences(app)
        preferences.edit().putString("app_theme", key).apply()
    }

    override fun switchNightMode(@AppCompatDelegate.NightMode key: Int) {
        val preferences = getPreferences(app)
        preferences.edit().putString("night_switch", key.toString()).apply()
    }

    override fun isDarkModeEnabled(context: Context): Boolean {
        val nightMode = (context.resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
        return nightMode == Configuration.UI_MODE_NIGHT_YES
    }

    @get:AppCompatDelegate.NightMode
    override val nightMode: Int
        get() = try {
            getPreferences(app)
                .getString("night_switch", AppCompatDelegate.MODE_NIGHT_YES.toString())!!
                .trim { it <= ' ' }
                .toInt()
        } catch (e: Exception) {
            AppCompatDelegate.MODE_NIGHT_YES
        }

    override val isDeveloper_mode: Boolean
        get() = getPreferences(app).getBoolean("developer_mode", forceDeveloperMode)

    override val isEnable_dirs_files_count: Boolean
        get() = getPreferences(app).getBoolean("enable_dirs_files_count", true)

    override val slidrSettings: SlidrSettings
        get() {
            val ret = getPreferences(app).getString("slidr_settings_json", null)
            return if (ret == null) {
                SlidrSettings().set_default()
            } else {
                kJson.decodeFromString(SlidrSettings.serializer(), ret)
            }
        }

    override fun setSlidrSettings(settings: SlidrSettings) {
        getPreferences(app).edit()
            .putString(
                "slidr_settings_json",
                kJson.encodeToString(SlidrSettings.serializer(), settings)
            ).apply()
    }

    override val maxBitmapResolution: Int
        get() = try {
            getPreferences(app).getString("max_bitmap_resolution", "4000")!!.trim()
                .toInt()
        } catch (e: Exception) {
            4000
        }

    override val maxThumbResolution: Int
        get() = try {
            getPreferences(app).getString("max_thumb_resolution", "384")!!.trim()
                .toInt()
        } catch (e: Exception) {
            384
        }

    override val rendering_mode: Int
        get() = try {
            getPreferences(app).getString("rendering_bitmap_mode", "0")!!.trim().toInt()
        } catch (e: Exception) {
            0
        }

    override val isInstant_photo_display: Boolean
        get() {
            if (!getPreferences(app).contains("instant_photo_display")) {
                getPreferences(app).edit().putBoolean(
                    "instant_photo_display",
                    ProjectNative.isNativeLoaded && FileUtils.threadsCount > 4
                ).apply()
            }
            return getPreferences(app).getBoolean("instant_photo_display", false)
        }

    override val photoExt: Set<String>
        get() = getPreferences(app)
            .getStringSet("photo_ext", setOf("gif", "jpg", "jpeg", "jpg", "webp", "png", "tiff"))!!

    override val audioExt: Set<String>
        get() = getPreferences(app)
            .getStringSet("audio_ext", setOf("mp3", "ogg", "flac", "opus"))!!

    @get:Transformers_Types
    override val viewpager_page_transform: Int
        get() = try {
            getPreferences(app).getString(
                "viewpager_page_transform",
                Transformers_Types.OFF.toString()
            )!!
                .trim { it <= ' ' }.toInt()
        } catch (e: Exception) {
            Transformers_Types.OFF
        }

    override val isLimitImage_cache: Int
        get() = try {
            getPreferences(app).getString("limit_cache_images", "2")!!
                .trim { it <= ' ' }.toInt()
        } catch (e: Exception) {
            2
        }

    override val picassoDispatcher: Int
        get() = try {
            if (!getPreferences(app).contains("picasso_dispatcher")) {
                getPreferences(app).edit().putString(
                    "picasso_dispatcher",
                    if (ProjectNative.isNativeLoaded && FileUtils.threadsCount > 4) "1" else "0"
                ).apply()
            }
            getPreferences(app).getString("picasso_dispatcher", "0")!!
                .trim { it <= ' ' }.toInt()
        } catch (e: Exception) {
            0
        }

    @get:Lang
    override val language: Int
        get() = try {
            getPreferences(app).getString("language_ui", "0")!!
                .trim { it <= ' ' }.toInt()
        } catch (e: Exception) {
            Lang.DEFAULT
        }

    override var volumeValue: Float
        get() = getPreferences(app).getFloat("volume_value", 7.5f)
        set(value) = getPreferences(app).edit().putFloat("volume_value", value)
            .apply()

    override var snakeScore: Int
        get() = getPreferences(app).getInt("snake_score", 0)
        set(value) = getPreferences(app).edit().putInt("snake_score", value)
            .apply()

    override var snakeVolumeValue: Int
        get() = getPreferences(app).getInt("snake_volume_value", 10)
        set(value) = getPreferences(app).edit().putInt("snake_volume_value", value)
            .apply()

    override var snakeRefreshMS: Int
        get() = getPreferences(app).getInt("snake_refresh_ms", 200)
        set(value) = getPreferences(app).edit().putInt("snake_refresh_ms", value)
            .apply()

    override var enableAccelerometer: Boolean
        get() = getPreferences(app).getBoolean("enable_accelerometer", true)
        set(value) = getPreferences(app).edit().putBoolean("enable_accelerometer", value)
            .apply()
}
