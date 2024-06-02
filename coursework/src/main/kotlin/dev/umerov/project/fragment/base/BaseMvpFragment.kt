package dev.umerov.project.fragment.base

import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import dev.umerov.project.Includes.provideApplicationContext
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityUtils.setToolbarSubtitle
import dev.umerov.project.activity.ActivityUtils.setToolbarTitle
import dev.umerov.project.fragment.base.compat.AbsMvpFragment
import dev.umerov.project.fragment.base.core.AbsPresenter
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.fragment.base.core.IToastView
import dev.umerov.project.fragment.base.core.IToolbarView
import dev.umerov.project.toColor
import dev.umerov.project.util.ErrorLocalizer.localizeThrowable
import dev.umerov.project.util.ViewUtils
import dev.umerov.project.util.toast.AbsCustomToast
import dev.umerov.project.util.toast.CustomSnackbars
import dev.umerov.project.util.toast.CustomToast.Companion.createCustomToast
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseMvpFragment<P : AbsPresenter<V>, V : IMvpView> : AbsMvpFragment<P, V>(),
    IMvpView, IErrorView, IToastView, IToolbarView {
    protected fun hasHideToolbarExtra(): Boolean {
        return arguments?.getBoolean(EXTRA_HIDE_TOOLBAR) == true
    }

    override fun showError(errorText: String?) {
        if (isAdded) {
            customToast?.showToastError(errorText)
        }
    }

    override fun showThrowable(throwable: Throwable?) {
        if (isAdded) {
            CustomSnackbars.createCustomSnackbars(view)?.let {
                val snack = it.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG).coloredSnack(
                    localizeThrowable(provideApplicationContext(), throwable),
                    "#eeff0000".toColor()
                )
                if (throwable !is SocketTimeoutException && throwable !is UnknownHostException) {
                    snack.setAction(R.string.more_info) {
                        val text = StringBuilder()
                        text.append(
                            localizeThrowable(
                                provideApplicationContext(),
                                throwable
                            )
                        )
                        text.append("\r\n")
                        for (stackTraceElement in (throwable ?: return@setAction).stackTrace) {
                            text.append("    ")
                            text.append(stackTraceElement)
                            text.append("\r\n")
                        }
                        MaterialAlertDialogBuilder(requireActivity())
                            .setIcon(R.drawable.ic_error)
                            .setMessage(text)
                            .setTitle(R.string.more_info)
                            .setPositiveButton(R.string.button_ok, null)
                            .setCancelable(true)
                            .show()
                    }
                }
                snack.show()
            } ?: showError(localizeThrowable(provideApplicationContext(), throwable))
        }
    }

    override val customToast: AbsCustomToast?
        get() = if (isAdded) {
            createCustomToast(requireActivity(), view)
        } else null

    override fun showError(@StringRes titleTes: Int, vararg params: Any?) {
        if (isAdded) {
            showError(getString(titleTes, *params))
        }
    }

    override fun setToolbarSubtitle(subtitle: String?) {
        setToolbarSubtitle(this, subtitle)
    }

    override fun setToolbarTitle(title: String?) {
        setToolbarTitle(this, title)
    }

    protected fun styleSwipeRefreshLayoutWithCurrentTheme(
        swipeRefreshLayout: SwipeRefreshLayout,
        needToolbarOffset: Boolean
    ) {
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(
            requireActivity(),
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

        fun safelySetVisibleOrGone(target: ViewGroup?, visible: Boolean) {
            target?.visibility = if (visible) View.VISIBLE else View.GONE
        }

        fun safelySetVisibleOrGoneView(target: View?, visible: Boolean) {
            target?.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
}
