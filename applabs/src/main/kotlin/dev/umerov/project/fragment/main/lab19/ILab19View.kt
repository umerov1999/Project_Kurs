package dev.umerov.project.fragment.main.lab19

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab19View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)
}
