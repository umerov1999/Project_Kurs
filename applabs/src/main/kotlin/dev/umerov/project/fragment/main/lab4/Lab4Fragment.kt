package dev.umerov.project.fragment.main.lab4

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem
import dev.umerov.project.toColor
import dev.umerov.project.util.Utils

class Lab4Fragment : BaseMvpFragment<Lab4Presenter, ILab4View>(),
    ILab4View {
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            if (requireArguments().getBoolean(Extra.IS_PROGRAM_LAYOUT, false)) {
                actionBar.setTitle(R.string.lab_4)
                actionBar.subtitle = null
            } else {
                actionBar.setTitle(R.string.lab_4_1)
                actionBar.subtitle = null
            }
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        Lab4Presenter(requireArguments().getBoolean(Extra.IS_PROGRAM_LAYOUT, false))

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View
        if (requireArguments().getBoolean(Extra.IS_PROGRAM_LAYOUT, false)) {
            root = LinearLayout(requireActivity())
            root.orientation = LinearLayout.VERTICAL
            root.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            for (i in 0..1) {
                val frame = FrameLayout(requireActivity())
                val v = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
                v.weight = 1F
                frame.layoutParams = v
                if (i == 0) {
                    frame.setBackgroundColor("#FFD5AE".toColor())
                } else {
                    frame.setBackgroundColor("#FFA5A2".toColor())
                }

                val grid = GridLayout(requireActivity())
                grid.columnCount = 2
                grid.rowCount = 2

                val vv = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                vv.gravity = Gravity.CENTER
                grid.layoutParams = vv

                for (s in 0..3) {
                    val button = MaterialButton(requireActivity())
                    button.text = (s + 1).toString()
                    val vvv = GridLayout.LayoutParams()
                    vvv.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    vvv.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    vvv.leftMargin = Utils.dp(4f)
                    vvv.rightMargin = Utils.dp(4f)
                    vvv.topMargin = Utils.dp(4f)
                    vvv.bottomMargin = Utils.dp(4f)
                    button.layoutParams = vvv
                    grid.addView(button)
                }

                frame.addView(grid)
                root.addView(frame)
            }
        } else {
            root = inflater.inflate(R.layout.fragment_lab4, container, false)
            (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        }
        return root
    }

    companion object {
        fun newInstance(isProgramLayout: Boolean): Lab4Fragment {
            val arg = Bundle()
            arg.putBoolean(Extra.IS_PROGRAM_LAYOUT, isProgramLayout)
            val fragment = Lab4Fragment()
            fragment.arguments = arg
            return fragment
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }
}
