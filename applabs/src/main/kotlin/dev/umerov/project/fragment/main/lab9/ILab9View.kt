package dev.umerov.project.fragment.main.lab9

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.labs.Lab9Contact

interface ILab9View : IMvpView, IErrorView {
    fun displayData(data: List<Lab9Contact>)
    fun showMessage(@StringRes res: Int)

    fun displayCreateDialog(obj: Lab9Contact)

    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDataRemoved(position: Int, count: Int)
}
