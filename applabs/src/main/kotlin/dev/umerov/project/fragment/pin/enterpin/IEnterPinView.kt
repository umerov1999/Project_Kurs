package dev.umerov.project.fragment.pin.enterpin

import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.fragment.base.core.IToastView

interface IEnterPinView : IMvpView, IErrorView, IToastView {
    fun displayPin(value: IntArray, noValue: Int)
    fun sendSuccessAndClose()
    fun displayErrorAnimation()
    fun showBiometricPrompt()
}