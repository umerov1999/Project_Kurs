package dev.umerov.project.fragment.main.lab5

import android.graphics.Color
import androidx.annotation.ColorInt
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter

class Lab5Presenter : RxSupportPresenter<ILab5View>() {
    private var isFirst = true

    @ColorInt
    private var currentColor: Int = Color.parseColor("#000000")
    private var updatingControl: Boolean = false

    fun fireUpdateRed(checked: Boolean) {
        if (updatingControl) {
            return
        }
        currentColor = Color.argb(
            255,
            if (checked) 255 else 0,
            Color.green(currentColor),
            Color.blue(currentColor)
        )
        view?.updateColor(currentColor, false)
    }

    fun fireUpdateGreen(checked: Boolean) {
        if (updatingControl) {
            return
        }
        currentColor = Color.argb(
            255,
            Color.red(currentColor),
            if (checked) 255 else 0,
            Color.blue(currentColor)
        )
        view?.updateColor(currentColor, false)
    }

    fun fireUpdateBlue(checked: Boolean) {
        if (updatingControl) {
            return
        }
        currentColor = Color.argb(
            255,
            Color.red(currentColor),
            Color.green(currentColor),
            if (checked) 255 else 0
        )
        view?.updateColor(currentColor, false)
    }

    override fun onGuiCreated(viewHost: ILab5View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_5)
            isFirst = false
        }
        updatingControl = true
        viewHost.updateColor(currentColor, true)
        updatingControl = false
    }
}
