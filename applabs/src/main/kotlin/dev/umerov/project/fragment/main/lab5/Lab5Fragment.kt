package dev.umerov.project.fragment.main.lab5

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem


class Lab5Fragment : BaseMvpFragment<Lab5Presenter, ILab5View>(),
    ILab5View {
    private var mRed: MaterialSwitch? = null
    private var mGreen: MaterialSwitch? = null
    private var mBlue: MaterialSwitch? = null
    private var mTitle: MaterialTextView? = null
    private var mGroup: ViewGroup? = null

    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_5)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab5Presenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab5, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        mRed = root.findViewById(R.id.item_red)
        mGreen = root.findViewById(R.id.item_green)
        mBlue = root.findViewById(R.id.item_blue)
        mTitle = root.findViewById(R.id.item_title)
        mGroup = root.findViewById(R.id.item_group)

        mRed?.setOnCheckedChangeListener { _, isChecked ->
            presenter?.fireUpdateRed(isChecked)
        }
        mGreen?.setOnCheckedChangeListener { _, isChecked ->
            presenter?.fireUpdateGreen(isChecked)
        }
        mBlue?.setOnCheckedChangeListener { _, isChecked ->
            presenter?.fireUpdateBlue(isChecked)
        }
        return root
    }

    companion object {
        fun newInstance(): Lab5Fragment {
            return Lab5Fragment()
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    override fun updateColor(@ColorInt color: Int, updateControl: Boolean) {
        if (updateControl) {
            mRed?.isChecked = Color.red(color) > 0
            mGreen?.isChecked = Color.green(color) > 0
            mBlue?.isChecked = Color.blue(color) > 0
        }
        mGroup?.setBackgroundColor(color)
        val inverted = Color.argb(
            255,
            255 - Color.red(color),
            255 - Color.green(color),
            255 - Color.blue(color)
        )
        mRed?.setTextColor(inverted)
        mGreen?.setTextColor(inverted)
        mBlue?.setTextColor(inverted)
        mTitle?.setTextColor(inverted)
    }
}
