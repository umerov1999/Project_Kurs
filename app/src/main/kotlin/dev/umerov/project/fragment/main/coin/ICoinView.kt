package dev.umerov.project.fragment.main.coin

import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.db.CoinOperation
import dev.umerov.project.model.db.Register

interface ICoinView : IMvpView, IErrorView {
    fun displayList(operations: List<CoinOperation>)
    fun displayRegister(register: Register?)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDataRemoved(position: Int, count: Int)
    fun displayLoading(loading: Boolean)

    fun displayCreateDialog(operation: CoinOperation)
}
