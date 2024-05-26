package dev.umerov.project.fragment.main.lab7

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.labs.Lab7Film

interface ILab7View : IMvpView, IErrorView {
    fun setData(list: List<Lab7Film>)
    fun showMessage(@StringRes res: Int)
}
