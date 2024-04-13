package dev.umerov.project

import dev.umerov.project.BuildConfig.DEBUG
import dev.umerov.project.BuildConfig.FORCE_DEVELOPER_MODE

object Constants {
    const val DATABASE_VERSION = 1

    const val EXPORT_SETTINGS_FORMAT = 1
    const val forceDeveloperMode = FORCE_DEVELOPER_MODE

    val IS_DEBUG: Boolean = DEBUG
    const val PIN_DIGITS_COUNT = 4

    const val DISABLED_RESOURCE_ID = -1
}