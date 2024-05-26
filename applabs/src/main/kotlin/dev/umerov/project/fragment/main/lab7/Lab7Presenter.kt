package dev.umerov.project.fragment.main.lab7

import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.model.main.labs.Lab7Film

class Lab7Presenter : RxSupportPresenter<ILab7View>() {
    private var isFirst = true

    private val list = ArrayList<Lab7Film>()

    override fun onGuiCreated(viewHost: ILab7View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_7)
            isFirst = false
        }
        viewHost.setData(list)
    }

    fun fireAdd(film: Lab7Film) {
        list.add(film)
        view?.setData(list)
    }
}
