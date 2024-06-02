package dev.umerov.project

import android.os.Build
import dev.umerov.project.util.Utils
import java.util.Locale

object Constants {
    const val DATABASE_VERSION = 6

    const val PICASSO_TAG = "picasso_tag"
    const val EXPORT_SETTINGS_FORMAT = 1
    const val forceDeveloperMode = BuildConfig.FORCE_DEVELOPER_MODE

    val IS_DEBUG: Boolean = BuildConfig.DEBUG
    const val PIN_DIGITS_COUNT = 4

    const val DISABLED_RESOURCE_ID = -1
    val USER_AGENT = String.format(
        Locale.US,
        "ProjectAndroid/%s-%s (Android %s; SDK %d; %s; %s; %s)",
        BuildConfig.VERSION_NAME,
        BuildConfig.VERSION_CODE,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT,
        Build.SUPPORTED_ABIS[0],
        Utils.deviceName,
        "ru"
    )
    const val FILE_PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.file_provider"

    const val API_TIMEOUT = 25L
    const val UPLOAD_TIMEOUT = 60L
    const val EXO_PLAYER_TIMEOUT = 60L
    const val DOWNLOAD_TIMEOUT = 3600L
    const val PICASSO_TIMEOUT = 15L
}
