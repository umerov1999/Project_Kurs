package dev.umerov.project.fragment.main.lab17

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ILab17View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)

    fun displayAnswer(text: String?)
}
