package dev.umerov.project.fragment.main.lab14

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.labs.Lab14AudioAlbum

interface ILab14View : IMvpView, IErrorView {
    fun displayData(data: List<Lab14AudioAlbum>)
    fun showMessage(@StringRes res: Int)

    fun displayCreateDialog(obj: Lab14AudioAlbum)

    fun notifyItemChanged(index: Int)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDataRemoved(position: Int, count: Int)

    fun updateCheckedItem(@IdRes checkedItem: Int)
}
