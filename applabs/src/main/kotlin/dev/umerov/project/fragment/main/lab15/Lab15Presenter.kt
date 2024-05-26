package dev.umerov.project.fragment.main.lab15

import android.util.Log
import androidx.fragment.app.FragmentActivity
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter

class Lab15Presenter(private val context: FragmentActivity) : RxSupportPresenter<ILab15View>() {
    private var isFirst = true
    private val thread = ThrExampleFour()
    override fun onGuiCreated(viewHost: ILab15View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_15)
            isFirst = false
        }
    }

    fun fireStart() {
        try {
            thread.start()
            view?.showMessage("Thread started successfully!")
        } catch (e: Exception) {
            view?.showThrowable(e)
        }
    }

    fun firePause() {
        try {
            thread.suspendWork()
            view?.showMessage("Thread suspended!")
        } catch (e: Exception) {
            view?.showThrowable(e)
        }
    }

    fun fireInterrupt() {
        try {
            thread.interrupt()
            view?.showMessage("Thread interrupted!")
        } catch (e: Exception) {
            view?.showThrowable(e)
        }
    }

    fun fireResume() {
        try {
            thread.resumeWork()
            view?.showMessage("Thread resumed!")
        } catch (e: Exception) {
            view?.showThrowable(e)
        }
    }

    inner class ThrExampleFour : Thread() {
        private var isPause = false
        private var isInterrupted = false
        override fun run() {
            try {
                var cnt = 0
                while (true) {
                    cnt++
                    printMessage("1. Baker \nkneads the dough. ($cnt)")
                    sleep(2000)
                    if (checkState()) {
                        return
                    }

                    printMessage("2. The baker opens the oven door. ($cnt)")
                    sleep(2000)
                    if (checkState()) {
                        return
                    }
                    printMessage(
                        "3. The baker opens the gas valve of the oven. ($cnt)"
                    )
                    sleep(2000)
                    printMessage(
                        "4. Baker lights a match and sets fireto the gas in the oven. ($cnt)"
                    )
                    sleep(2000)
                    if (checkState()) {
                        return
                    }
                    printMessage(
                        "5. Baker puts the dough in the oven. (" +
                                cnt + ")"
                    )
                    sleep(2000)
                    printMessage(
                        "6. The baker closes the oven door." +
                                "Baking started. (" + cnt + ")"
                    )
                    sleep(2000)
                    if (checkState()) {
                        return
                    }
                }
            } catch (ie: InterruptedException) {
                this.printMessage("Thread Interrupted : " + ie.message)
            }
        }

        @Synchronized
        fun suspendWork() {
            if (isInterrupted) {
                return
            }
            this.isPause = true
        }

        @Synchronized
        fun resumeWork() {
            if (isInterrupted) {
                return
            }
            this.isPause = false
            (this as Object).notify()
        }

        @Synchronized
        @Throws(InterruptedException::class)
        private fun checkState(): Boolean {
            if (isInterrupted) {
                printMessage("Terminating thread")
                super.interrupt()
                return true
            }
            while (this.isPause) {
                Log.d(THR_TAG, "Thread Suspended")
                (this as Object).wait()
            }
            return false
        }

        @Synchronized
        @Throws(InterruptedException::class)
        override fun interrupt() {
            isInterrupted = true
            if (isPause) {
                super.interrupt()
                isPause = false
            }
        }

        @Synchronized
        @Throws(InterruptedException::class)
        override fun start() {
            if (isPause) {
                return
            }
            isInterrupted = false
            super.start()
        }

        private fun printMessage(msg: String) {
            Log.d(THR_TAG, msg)
            context.runOnUiThread {
                view?.printText(msg)
            }
        }
    }

    override fun destroy() {
        super.destroy()
        thread.interrupt()
    }

    companion object {
        private val THR_TAG = ThrExampleFour::class.simpleName.orEmpty()
    }
}
