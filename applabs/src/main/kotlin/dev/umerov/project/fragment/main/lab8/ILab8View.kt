package dev.umerov.project.fragment.main.lab8

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.labs.Lab8Currency

interface ILab8View : IMvpView, IErrorView {
    fun showMessage(@StringRes res: Int)

    fun display(
        currencyList: List<Lab8Currency>,
        currencySelection: Int,
        convertToSelection: Int,
        isPurchase: Boolean,
        exchangeRateText: String,
        sum: String?,
        resultText: String?
    )

    fun updateExchangeRateText(exchangeRateText: String)
    fun updateSum(sum: String?)
    fun updateResult(resultText: String?)
}
