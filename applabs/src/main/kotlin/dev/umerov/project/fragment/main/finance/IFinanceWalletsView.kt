package dev.umerov.project.fragment.main.finance

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.labs.FinanceWallet

interface IFinanceWalletsView : IMvpView, IErrorView {
    fun displayData(data: List<FinanceWallet>)
    fun showMessage(@StringRes res: Int)

    fun displayCreateDialog(obj: FinanceWallet)

    fun notifyItemChanged(index: Int)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDataRemoved(position: Int, count: Int)

    fun showOperationFragment(ownerId: Long)
}
