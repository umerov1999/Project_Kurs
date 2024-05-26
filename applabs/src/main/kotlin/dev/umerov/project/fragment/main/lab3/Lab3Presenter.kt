package dev.umerov.project.fragment.main.lab3

import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter

class Lab3Presenter : RxSupportPresenter<ILab3View>() {
    private var isFirst = true

    private var res: Double = 0.0
    private var tmpString: String = "0"
    private var action: Int = -1

    override fun onGuiCreated(viewHost: ILab3View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_3)
            isFirst = false
        }
        viewHost.displayValue(tmpString)
    }

    fun doAction(action: Int) {
        try {
            if (this.action == -1) {
                res = tmpString.toDouble()
            } else {
                when (this.action) {
                    0 -> {
                        res -= tmpString.toDouble()
                    }

                    1 -> {
                        res += tmpString.toDouble()
                    }

                    2 -> {
                        res *= tmpString.toDouble()
                    }

                    3 -> {
                        res /= tmpString.toDouble()
                    }
                }
            }
            tmpString = "0"
            this.action = action
            view?.displayValue(tmpString)
        } catch (e: Exception) {
            view?.showError(e.localizedMessage)
        }
    }

    fun ravno() {
        try {
            if (this.action == -1) {
                tmpString = res.toString()
            } else {
                when (this.action) {
                    0 -> {
                        res -= tmpString.toDouble()
                    }

                    1 -> {
                        res += tmpString.toDouble()
                    }

                    2 -> {
                        res *= tmpString.toDouble()
                    }

                    3 -> {
                        res /= tmpString.toDouble()
                    }
                }
            }
            tmpString = res.toString()
            this.action = -1
            view?.displayValue(tmpString)
        } catch (e: Exception) {
            view?.showError(e.localizedMessage)
        }
    }

    fun fireAdd(value: String) {
        if (value == "-" && tmpString != "0") {
            doAction(0)
            return
        }
        if (value == "-" && tmpString.contains("-") || value == "." && tmpString.contains(".")) {
            return
        }
        if (tmpString == "0" && value != ".") {
            tmpString = value
        } else {
            tmpString += value
        }
        view?.displayValue(tmpString)
    }

    fun fireBackSpaceString() {
        var tmp = ""
        for (i in 0..tmpString.length - 2) {
            tmp += tmpString[i]
        }
        tmpString = tmp
        if (tmpString.isEmpty()) {
            tmpString = "0"
        }
        view?.displayValue(tmpString)
    }

    fun fireReset() {
        res = 0.0
        action = -1
        tmpString = "0"
        view?.displayValue(tmpString)
    }
}
