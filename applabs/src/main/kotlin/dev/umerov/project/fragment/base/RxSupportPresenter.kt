package dev.umerov.project.fragment.base

import android.content.Context
import androidx.annotation.StringRes
import dev.umerov.project.App.Companion.instance
import dev.umerov.project.Constants
import dev.umerov.project.Includes.provideApplicationContext
import dev.umerov.project.fragment.base.core.AbsPresenter
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.settings.Settings.get
import dev.umerov.project.util.ErrorLocalizer
import dev.umerov.project.util.Utils
import dev.umerov.project.util.coroutines.CompositeJob
import kotlinx.coroutines.Job

abstract class RxSupportPresenter<V : IMvpView> :
    AbsPresenter<V>() {
    protected val compositeJob = CompositeJob()
    var viewCreationCount = 0
        private set

    override fun onGuiCreated(viewHost: V) {
        viewCreationCount++
        super.onGuiCreated(viewHost)
    }

    override fun onDestroyed() {
        compositeJob.cancel()
        super.onDestroyed()
    }

    fun appendJob(job: Job) {
        compositeJob.add(job)
    }

    protected fun showError(view: IErrorView?, throwable: Throwable?) {
        view ?: return
        throwable ?: return
        val lThrowable = Utils.getCauseIfRuntime(throwable)
        if (Constants.IS_DEBUG) {
            lThrowable.printStackTrace()
        }
        if (get().main().isDeveloper_mode) {
            view.showThrowable(lThrowable)
        } else {
            view.showError(ErrorLocalizer.localizeThrowable(applicationContext, lThrowable))
        }
    }

    protected val applicationContext: Context
        get() = provideApplicationContext()

    protected fun getString(@StringRes res: Int): String {
        return instance.getString(res)
    }

    protected fun getString(@StringRes res: Int, vararg params: Any?): String {
        return instance.getString(res, *params)
    }
}
