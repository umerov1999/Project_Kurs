package dev.umerov.project.fragment.main.balance

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.db.Register

interface IBalanceView : IMvpView, IErrorView {
    fun displayData(register: Register?)
    fun showMessage(@StringRes res: Int)
}
