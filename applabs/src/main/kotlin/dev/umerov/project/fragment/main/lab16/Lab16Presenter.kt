package dev.umerov.project.fragment.main.lab16

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.annotation.IdRes
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter

class Lab16Presenter : RxSupportPresenter<ILab16View>() {
    private var isFirst = true
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            view?.setSize(
                msg.data.getInt("id"),
                msg.data.getInt("width"),
                msg.data.getInt("height")
            )
        }
    }
    private val thread1 = MyThrResizer(handler, R.id.button_test1, true)
    private val thread2 = MyThrResizer(handler, R.id.button_test2, false)
    override fun onGuiCreated(viewHost: ILab16View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_16)
            isFirst = false
        }
    }

    fun fireStart() {
        try {
            thread1.start()
            thread2.start()
            view?.showMessage("Thread started successfully!")
        } catch (e: Exception) {
            view?.showThrowable(e)
        }
    }

    fun firePause() {
        try {
            thread1.suspendWork()
            thread2.suspendWork()
            view?.showMessage("Thread suspended!")
        } catch (e: Exception) {
            view?.showThrowable(e)
        }
    }

    fun fireInterrupt() {
        try {
            thread1.interrupt()
            thread2.interrupt()
            view?.showMessage("Thread interrupted!")
        } catch (e: Exception) {
            view?.showThrowable(e)
        }
    }

    fun fireResume() {
        try {
            thread1.resumeWork()
            thread2.resumeWork()
            view?.showMessage("Thread resumed!")
        } catch (e: Exception) {
            view?.showThrowable(e)
        }
    }

    class MyThrResizer(
        private val handler: Handler,
        @IdRes private val rId: Int,
        private var isForward: Boolean = true
    ) : Thread() {
        private var isPause = false
        private var isInterrupted = false

        private var width = if (!isForward) 400 else 200
        private var height = if (!isForward) 300 else 100
        override fun run() {
            try {
                while (true) {
                    sleep(20)
                    if (checkState()) {
                        return
                    }
                    if (isForward) {
                        width++; height++
                        if (width > 400) isForward = false
                    } else {
                        width--; height--
                        if (width < 200) isForward = true
                    }

                    sendSize()

                    if (checkState()) {
                        return
                    }
                }
            } catch (_: InterruptedException) {
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
                super.interrupt()
                return true
            }
            while (this.isPause) {
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

        private fun sendSize() {
            val msg = Message()
            msg.data.putInt("id", rId)
            msg.data.putInt("width", width)
            msg.data.putInt("height", height)
            handler.sendMessage(msg)
        }
    }

    override fun destroy() {
        super.destroy()
        thread1.interrupt()
        thread2.interrupt()
    }
}
