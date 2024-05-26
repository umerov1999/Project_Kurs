package dev.umerov.project.fragment.main.lab1

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab1View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)
}
