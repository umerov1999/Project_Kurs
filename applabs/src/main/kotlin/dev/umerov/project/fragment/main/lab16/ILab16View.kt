package dev.umerov.project.fragment.main.lab16

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab16View : IMvpView, IErrorView {
    fun showMessage(str: String?)
    fun showMessage(@StringRes res: Int)
    fun setSize(@IdRes rId: Int, width: Int, height: Int)
}
