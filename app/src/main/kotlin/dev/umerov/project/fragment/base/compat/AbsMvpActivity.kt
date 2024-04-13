package dev.umerov.project.fragment.base.compat

import android.os.Bundle
import dev.umerov.project.activity.NoMainActivity
import dev.umerov.project.fragment.base.core.IErrorView
import dev.umerov.project.fragment.base.core.IMvpView
import dev.umerov.project.fragment.base.core.IPresenter
import dev.umerov.project.fragment.base.core.IToastView
import dev.umerov.project.fragment.base.core.IToolbarView

abstract class AbsMvpActivity<P : IPresenter<V>, V : IMvpView> : NoMainActivity(),
    ViewHostDelegate.IFactoryProvider<P, V>, IErrorView,
    IToastView, IToolbarView {

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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delegate.onViewCreated()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        delegate.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        delegate.onPause()
    }

    override fun onResume() {
        super.onResume()
        delegate.onResume()
    }

    override fun onDestroy() {
        delegate.onDestroyView()
        delegate.onDestroy()
        super.onDestroy()
    }

    fun lazyPresenter(block: P.() -> Unit) {
        delegate.lazyPresenter(block)
    }
}
