package dev.umerov.project.settings

import dev.umerov.project.Includes

object Settings {
    fun get(): ISettings {
        return Includes.settings
    }
}