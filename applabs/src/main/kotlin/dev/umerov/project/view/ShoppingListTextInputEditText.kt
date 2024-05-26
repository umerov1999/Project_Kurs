package dev.umerov.project.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.widget.ArrayAdapter
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dev.umerov.project.Includes
import dev.umerov.project.R
import dev.umerov.project.fromIOToMain
import dev.umerov.project.getParcelableCompat
import io.reactivex.rxjava3.disposables.Disposable

class ShoppingListTextInputEditText : MaterialAutoCompleteTextView {
    private var listQueries = ArrayList<String?>()
    private var mQueryDisposable = Disposable.disposed()
    private var isFetchedListQueries = false
    private var versionTmp: Int = version

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) {
            return
        }
        if (!isFetchedListQueries || versionTmp != version) {
            versionTmp = version
            loadQueries()
        } else {
            updateQueriesAdapter()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mQueryDisposable.dispose()
    }

    private fun loadQueries() {
        mQueryDisposable.dispose()
        mQueryDisposable = Includes.stores.shoppingStore().getShoppingListHelper()
            .fromIOToMain()
            .subscribe({ s ->
                isFetchedListQueries = true
                listQueries.clear()
                listQueries.addAll(s)
                updateQueriesAdapter()
            }, { Log.e(TAG, it.localizedMessage.orEmpty()) })
    }

    private fun updateQueriesAdapter() {
        val array = Array(listQueries.size) { listQueries[it] }
        val spinnerItems = ArrayAdapter(
            context,
            R.layout.search_dropdown_item,
            array
        )
        setAdapter(spinnerItems)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val state = Bundle()
        state.putParcelable("PARENT", superState)
        state.putStringArrayList("listQueries", listQueries)
        state.putBoolean("isFetchedListQueries", isFetchedListQueries)
        state.putInt("version", versionTmp)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as Bundle
        val superState = savedState.getParcelableCompat<Parcelable>("PARENT")
        super.onRestoreInstanceState(superState)

        listQueries.clear()
        savedState.getStringArrayList("listQueries")?.let { listQueries.addAll(it) }
        isFetchedListQueries = state.getBoolean("isFetchedListQueries")
        versionTmp = state.getInt("version")
    }

    companion object {
        private var version: Int = 0
        fun incrementVersion() {
            version++
        }

        private val TAG = ShoppingListTextInputEditText::class.simpleName.orEmpty()
    }
}
