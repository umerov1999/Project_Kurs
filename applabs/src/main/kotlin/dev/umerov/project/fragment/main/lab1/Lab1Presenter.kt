package dev.umerov.project.fragment.main.lab1

import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter

class Lab1Presenter : RxSupportPresenter<ILab1View>() {
    private var isFirst = true
    override fun onGuiCreated(viewHost: ILab1View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_1)
            isFirst = false
        }
    }
}