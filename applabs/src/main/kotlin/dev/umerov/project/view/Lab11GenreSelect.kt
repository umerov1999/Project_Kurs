package dev.umerov.project.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dev.umerov.project.Includes
import dev.umerov.project.R
import dev.umerov.project.getParcelableArrayListCompat
import dev.umerov.project.getParcelableCompat
import dev.umerov.project.model.main.labs.Lab11Genre
import dev.umerov.project.util.coroutines.CancelableJob
import dev.umerov.project.util.coroutines.CoroutinesUtils
import dev.umerov.project.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.umerov.project.util.coroutines.CoroutinesUtils.sharedFlowToMain

class Lab11GenreSelect : FrameLayout {
    private var disposable = CancelableJob()
    private val list = ArrayList<Lab11Genre>()
    var selected: Lab11Genre? = null
        private set
    private var requested = false
    private var restored = false
    private lateinit var root: View
    private var isAttached = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    ) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(context)
    }

    @SuppressLint("CheckResult")
    private fun init(context: Context) {
        root = LayoutInflater.from(context).inflate(R.layout.entry_lab11_select_genre, this)
        if (!isInEditMode) {
            changedObserve.sharedFlowToMain {
                if (isAttached) {
                    fetchList()
                } else {
                    requested = false
                }
            }
        }
    }

    fun setSelected(selected: Lab11Genre?) {
        this.selected = selected
        updateElements()
    }

    private fun updateElements() {
        val array = Array(list.size) { list[it].name }
        val spinnerItems = ArrayAdapter(
            context,
            R.layout.lab11_genre_spinner_item,
            array
        )
        if (selected != null) {
            var tmpPos = -1
            for (i in list.indices) {
                if (list[i].db_id == selected?.db_id) {
                    tmpPos = i
                    break
                }
            }
            if (tmpPos >= 0) {
                root.findViewById<MaterialAutoCompleteTextView>(R.id.genre_selected)
                    .setText(spinnerItems.getItem(tmpPos))
            }
        }
        root.findViewById<MaterialAutoCompleteTextView>(R.id.genre_selected)
            .setAdapter(spinnerItems)
        root.findViewById<MaterialAutoCompleteTextView>(R.id.genre_selected)
            .setOnItemClickListener { _, _, position, _ ->
                if (position >= 0 && list.size > position) {
                    selected = list[position]
                }
            }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val state = Bundle()
        state.putParcelable("PARENT", superState)
        state.putParcelableArrayList("list", list)
        state.putBoolean("requested", requested)
        state.putParcelable("selected", selected)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as Bundle
        val superState = savedState.getParcelableCompat<Parcelable>("PARENT")
        super.onRestoreInstanceState(superState)

        list.clear()
        savedState.getParcelableArrayListCompat<Lab11Genre>("list")?.let { list.addAll(it) }
        requested = state.getBoolean("requested")
        selected = state.getParcelableCompat("selected")
        restored = true
    }

    private fun fetchList() {
        disposable += Includes.stores.projectStore().getGenres().fromIOToMain({
            list.clear()
            list.addAll(it)
            updateElements()
        }, { Log.e("Lab11GenreSelect", it.localizedMessage.orEmpty()) })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) {
            return
        }
        isAttached = true
        if (!requested) {
            requested = true
            fetchList()
        } else if (restored) {
            restored = false
            updateElements()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isInEditMode) {
            return
        }
        disposable.cancel()
        isAttached = false
    }

    companion object {
        val changedObserve = CoroutinesUtils.createPublishSubject<Boolean>()
    }
}
