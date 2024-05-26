package dev.umerov.project.activity.lab13

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab13View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)
}
