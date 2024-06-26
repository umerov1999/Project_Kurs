package dev.umerov.project.fragment.base.compat

import android.os.Bundle
import android.view.View
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.fragment.base.core.IPresenter

abstract class AbsMvpDialogFragment<P : IPresenter<V>, V : IMvpView> :
    androidx.fragment.app.DialogFragment(), ViewHostDelegate.IFactoryProvider<P, V> {

    private val delegate = ViewHostDelegate<P, V>()

    protected val presenter: P?
        get() = delegate.presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.onCreate(
            getViewHost(),
            this,
            this,
            savedInstanceState
        )
    }

    // Override in case of fragment not implementing IPresenter<View> interface
    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    private fun getViewHost(): V = this as V

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        delegate.onViewCreated()
    }

    protected fun fireViewCreated() {
        delegate.onViewCreated()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        delegate.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        delegate.onRestoreViewState()
    }

    override fun onPause() {
        super.onPause()
        delegate.onPause()
    }

    override fun onResume() {
        super.onResume()
        delegate.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        delegate.onDestroyView()
    }

    fun lazyPresenter(block: P.() -> Unit) {
        delegate.lazyPresenter(block)
    }

    override fun onDestroy() {
        delegate.onDestroy()
        super.onDestroy()
    }

}
