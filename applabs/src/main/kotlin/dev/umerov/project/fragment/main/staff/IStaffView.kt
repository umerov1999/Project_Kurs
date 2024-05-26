package dev.umerov.project.fragment.main.staff

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.Human

interface IStaffView : IMvpView, IErrorView {
    fun displayData(data: List<Human>)
    fun showMessage(@StringRes res: Int)

    fun displayCreateDialog(obj: Human)

    fun notifyDataSetChanged()
    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDataRemoved(position: Int, count: Int)
}
