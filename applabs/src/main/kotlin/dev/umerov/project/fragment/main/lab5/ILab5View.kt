package dev.umerov.project.fragment.main.lab5

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab5View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)
    fun updateColor(@ColorInt color: Int, updateControl: Boolean)
}
