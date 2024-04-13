package dev.umerov.project.fragment.theme

import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.settings.theme.ThemeValue

interface IThemeView : IMvpView {
    fun displayData(data: Array<ThemeValue>)
}