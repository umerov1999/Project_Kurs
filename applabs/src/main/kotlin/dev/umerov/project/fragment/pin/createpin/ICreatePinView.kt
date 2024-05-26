package dev.umerov.project.fragment.pin.createpin

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView

interface ICreatePinView : IMvpView, IErrorView {
    fun displayTitle(@StringRes titleRes: Int)
    fun displayErrorAnimation()
    fun displayPin(value: IntArray, noValue: Int)
    fun sendSuccessAndClose(values: IntArray)
}