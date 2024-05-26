package dev.umerov.project.fragment.main.lab10

import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter

class Lab10Presenter : RxSupportPresenter<ILab10View>() {
    private var isFirst = true
    override fun onGuiCreated(viewHost: ILab10View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_10)
            isFirst = false
        }
    }
}
