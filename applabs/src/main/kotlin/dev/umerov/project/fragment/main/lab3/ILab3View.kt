package dev.umerov.project.fragment.main.lab3

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab3View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)

    fun displayValue(res: String)
}
