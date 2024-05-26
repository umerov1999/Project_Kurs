package dev.umerov.project.fragment.main.shoppinglist

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.labs.ShoppingList

interface IShoppingListView : IMvpView, IErrorView {
    fun displayData(data: List<ShoppingList>)
    fun showMessage(@StringRes res: Int)

    fun displayCreateDialog(obj: ShoppingList)
    fun showProductFragment(shoppingList: ShoppingList)

    fun notifyItemChanged(index: Int)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDataRemoved(position: Int, count: Int)
}
