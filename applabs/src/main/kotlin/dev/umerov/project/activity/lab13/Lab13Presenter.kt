package dev.umerov.project.activity.lab13

import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter

class Lab13Presenter : RxSupportPresenter<ILab13View>() {
    private var isFirst = true
    override fun onGuiCreated(viewHost: ILab13View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_13)
            isFirst = false
        }
    }
}