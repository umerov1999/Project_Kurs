package dev.umerov.project.fragment.main

import androidx.annotation.StringRes
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.model.main.MainButton
import dev.umerov.project.place.Place

interface IMainView : IMvpView, IErrorView {
    fun displayData(data: Array<MainButton>)
    fun showMessage(@StringRes res: Int)

    fun openPlace(place: Place)
}
