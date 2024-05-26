package dev.umerov.project.fragment.main.lab2

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab2View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)
    fun updateColor(@ColorInt color: Int)
}
