package dev.umerov.project.fragment.main.lab6

import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter

class Lab6Presenter : RxSupportPresenter<ILab6View>() {
    private var isFirst = true

    override fun onGuiCreated(viewHost: ILab6View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_6)
            isFirst = false
        }
    }
}
