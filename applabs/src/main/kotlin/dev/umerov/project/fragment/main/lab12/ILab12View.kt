package dev.umerov.project.fragment.main.lab12

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab12View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)
    fun showTexts(raw: String?, asset: String?)
}
