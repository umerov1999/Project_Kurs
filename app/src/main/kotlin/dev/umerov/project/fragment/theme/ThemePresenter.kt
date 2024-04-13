package dev.umerov.project.fragment.theme

import android.os.Bundle
import dev.umerov.project.fragment.base.core.AbsPresenter
import dev.umerov.project.settings.theme.ThemesController

class ThemePresenter(savedInstanceState: Bundle?) : AbsPresenter<IThemeView>(savedInstanceState) {
    override fun onGuiCreated(viewHost: IThemeView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(ThemesController.themes)
    }
}