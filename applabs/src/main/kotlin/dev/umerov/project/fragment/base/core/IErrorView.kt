package dev.umerov.project.fragment.base.core

import androidx.annotation.StringRes

interface IErrorView {
    fun showError(errorText: String?)
    fun showThrowable(throwable: Throwable?)
    fun showError(@StringRes titleTes: Int, vararg params: Any?)
}
