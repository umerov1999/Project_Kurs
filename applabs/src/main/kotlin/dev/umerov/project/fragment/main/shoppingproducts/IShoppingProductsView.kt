package dev.umerov.project.fragment.main.shoppingproducts

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.labs.Product

interface IShoppingProductsView : IMvpView, IErrorView {
    fun displayData(data: List<Product>)
    fun showMessage(@StringRes res: Int)

    fun displayCreateDialog(obj: Product)

    fun notifyItemChanged(index: Int)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDataRemoved(position: Int, count: Int)
    fun notifyItemMoved(fromPosition: Int, toPosition: Int)
}
