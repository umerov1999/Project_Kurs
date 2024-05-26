package dev.umerov.project.fragment.theme

import dev.umerov.project.fragment.base.core.AbsPresenter
import dev.umerov.project.settings.theme.ThemesController

class ThemePresenter : AbsPresenter<IThemeView>() {
    override fun onGuiCreated(viewHost: IThemeView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(ThemesController.themes)
    }
}