package dev.umerov.project.settings.backup

import androidx.annotation.Keep
import de.maxr1998.modernpreferences.PreferenceScreen
import dev.umerov.project.Includes
import dev.umerov.project.kJson
import dev.umerov.project.util.serializeble.json.JsonObject
import dev.umerov.project.util.serializeble.json.JsonObjectBuilder
import dev.umerov.project.util.serializeble.prefs.Preferences
import kotlinx.serialization.Serializable

class SettingsBackup {
    @Keep
    @Serializable
    @Suppress("unused")
    class AppPreferencesList {
        //Main
        var app_theme: String? = null
        var night_switch: String? = null
        var theme_overlay: String? = null
        var language_ui: String? = null
        var font_size_int: Int? = null
        var slidr_settings: String? = null
        var developer_mode: Boolean? = null
        var viewpager_page_transform: String? = null
    }

    fun doBackup(): JsonObject {
        val pref =
            PreferenceScreen.getPreferences(Includes.provideApplicationContext())
        val preferences = Preferences(pref)
        val ret = JsonObjectBuilder()
        ret.put(
            "app",
            kJson.encodeToJsonElement(
                AppPreferencesList.serializer(),
                preferences.decode(AppPreferencesList.serializer(), "")
            )
        )
        return ret.build()
    }

    fun doRestore(ret: JsonObject?) {
        ret ?: return
        val pref =
            PreferenceScreen.getPreferences(Includes.provideApplicationContext())

        val preferences = Preferences(pref)

        ret["app"]?.let {
            preferences.encode(
                AppPreferencesList.serializer(),
                "",
                kJson.decodeFromJsonElement(AppPreferencesList.serializer(), it)
            )
        }
    }
}
