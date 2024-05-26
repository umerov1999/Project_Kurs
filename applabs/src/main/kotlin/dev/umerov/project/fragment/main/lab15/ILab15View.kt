package dev.umerov.project.fragment.main.lab15

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab15View : IMvpView, IErrorView {
    fun showMessage(str: String?)
    fun showMessage(@StringRes res: Int)
    fun printText(str: String?)
}
