package dev.umerov.project.activity

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.umerov.project.Includes.provideApplicationContext
import dev.umerov.project.fragment.base.compat.AbsMvpActivity
import dev.umerov.project.fragment.base.core.AbsPresenter
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.util.ErrorLocalizer.localizeThrowable
import dev.umerov.project.util.ViewUtils
import dev.umerov.project.util.toast.AbsCustomToast
import dev.umerov.project.util.toast.CustomToast

abstract class BaseMvpActivity<P : AbsPresenter<V>, V : IMvpView> : AbsMvpActivity<P, V>(),
    IMvpView {
    protected val arguments: Bundle?
        get() = intent?.extras

    protected fun requireArguments(): Bundle {
        return intent!!.extras!!
    }

    override fun showError(errorText: String?) {
        if (!isFinishing) {
            customToast?.showToastError(errorText)
        }
    }

    override fun showThrowable(throwable: Throwable?) {
        if (!isFinishing) {
            showError(localizeThrowable(provideApplicationContext(), throwable))
        }
    }

    override val customToast: AbsCustomToast?
        get() = if (!isFinishing) {
            CustomToast.createCustomToast(this, null)
        } else null

    override fun showError(@StringRes titleTes: Int, vararg params: Any?) {
        if (!isFinishing) {
            showError(getString(titleTes, *params))
        }
    }

    override fun setToolbarSubtitle(subtitle: String?) {
        supportActionBar?.subtitle = subtitle
    }

    override fun setToolbarTitle(title: String?) {
        supportActionBar?.title = title
    }

    protected fun styleSwipeRefreshLayoutWithCurrentTheme(
        swipeRefreshLayout: SwipeRefreshLayout,
        needToolbarOffset: Boolean
    ) {
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(
            this,
            swipeRefreshLayout,
            needToolbarOffset
        )
    }

    companion object {
        const val EXTRA_HIDE_TOOLBAR = "extra_hide_toolbar"
        fun safelySetChecked(button: CompoundButton?, checked: Boolean) {
            button?.isChecked = checked
        }

        fun safelySetText(target: TextView?, text: String?) {
            target?.text = text
        }

        fun safelySetText(target: TextView?, @StringRes text: Int) {
            target?.setText(text)
        }

        fun safelySetVisibleOrGone(target: View?, visible: Boolean) {
            target?.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
}
