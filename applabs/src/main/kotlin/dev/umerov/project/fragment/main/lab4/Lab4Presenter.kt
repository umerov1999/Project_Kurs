package dev.umerov.project.fragment.main.lab4

import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter

class Lab4Presenter(private val isProgramLayout: Boolean) : RxSupportPresenter<ILab4View>() {
    private var isFirst = true
    override fun onGuiCreated(viewHost: ILab4View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            if (isProgramLayout) {
                viewHost.showMessage(R.string.lab_4_1)
            } else {
                viewHost.showMessage(R.string.lab_4)
            }
            isFirst = false
        }
    }
}
