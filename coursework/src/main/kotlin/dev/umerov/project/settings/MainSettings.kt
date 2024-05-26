package dev.umerov.project.settings

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import de.maxr1998.modernpreferences.PreferenceScreen.Companion.getPreferences
import dev.umerov.project.Constants.forceDeveloperMode
import dev.umerov.project.kJson
import dev.umerov.project.model.Lang
import dev.umerov.project.model.SlidrSettings
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

    @get:Lang
    override val language: Int
        get() = try {
            getPreferences(app).getString("language_ui", "0")!!
                .trim { it <= ' ' }.toInt()
        } catch (e: Exception) {
            Lang.DEFAULT
        }
}