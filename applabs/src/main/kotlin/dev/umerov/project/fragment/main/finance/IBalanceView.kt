package dev.umerov.project.fragment.main.finance

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.labs.FinanceBalance

interface IBalanceView : IMvpView, IErrorView {
    fun displayData(financeBalance: FinanceBalance?)
    fun showMessage(@StringRes res: Int)
}
