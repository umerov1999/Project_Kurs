package dev.umerov.project.fragment.main.lab4

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab4View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)
}
