package dev.umerov.project.fragment.main.lab11

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.labs.Lab11Genre

interface ILab11GenresView : IMvpView, IErrorView {
    fun displayData(data: List<Lab11Genre>)
    fun showMessage(@StringRes res: Int)

    fun displayCreateDialog(obj: Lab11Genre)

    fun notifyItemChanged(index: Int)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDataRemoved(position: Int, count: Int)
}
