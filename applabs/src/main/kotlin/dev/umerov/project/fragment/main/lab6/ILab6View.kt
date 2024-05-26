package dev.umerov.project.fragment.main.lab6

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab6View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)
}
