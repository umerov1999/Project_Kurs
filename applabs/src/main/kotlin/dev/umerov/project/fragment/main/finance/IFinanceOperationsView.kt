package dev.umerov.project.fragment.main.finance

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.labs.FinanceOperation

interface IFinanceOperationsView : IMvpView, IErrorView {
    fun displayData(data: List<FinanceOperation>)
    fun showMessage(@StringRes res: Int)

    fun displayCreateDialog(obj: FinanceOperation)

    fun notifyItemChanged(index: Int)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDataRemoved(position: Int, count: Int)
}
