package dev.umerov.project.fragment.main.lab3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.util.Utils
import dev.umerov.project.view.natives.rlottie.RLottieImageView

class Lab3Fragment : BaseMvpFragment<Lab3Presenter, ILab3View>(),
    ILab3View {
    private var resultText: MaterialTextView? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_3)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab3Presenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab3, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        resultText = root.findViewById(R.id.item_result_text)

        val anim: RLottieImageView = root.findViewById(R.id.lottie_animation)
        if (ProjectNative.isNativeLoaded) {
            anim.fromRes(
                R.raw.project,
                Utils.dp(108f),
                Utils.dp(108f),
                intArrayOf(
                    0x000000,
                    CurrentTheme.getColorPrimary(requireActivity()),
                    0xffffff,
                    CurrentTheme.getColorSecondary(requireActivity())
                )
            )
            anim.playAnimation()
        }

        root.findViewById<MaterialButton>(R.id.item_ac).setOnClickListener {
            presenter?.fireReset()
        }

        root.findViewById<MaterialButton>(R.id.item_back).setOnClickListener {
            presenter?.fireBackSpaceString()
        }

        root.findViewById<MaterialButton>(R.id.item_z).setOnClickListener {
            presenter?.fireAdd(".")
        }

        root.findViewById<MaterialButton>(R.id.item_0).setOnClickListener {
            presenter?.fireAdd("0")
        }

        root.findViewById<MaterialButton>(R.id.item_1).setOnClickListener {
            presenter?.fireAdd("1")
        }

        root.findViewById<MaterialButton>(R.id.item_2).setOnClickListener {
            presenter?.fireAdd("2")
        }

        root.findViewById<MaterialButton>(R.id.item_3).setOnClickListener {
            presenter?.fireAdd("3")
        }

        root.findViewById<MaterialButton>(R.id.item_4).setOnClickListener {
            presenter?.fireAdd("4")
        }

        root.findViewById<MaterialButton>(R.id.item_5).setOnClickListener {
            presenter?.fireAdd("5")
        }

        root.findViewById<MaterialButton>(R.id.item_6).setOnClickListener {
            presenter?.fireAdd("6")
        }

        root.findViewById<MaterialButton>(R.id.item_7).setOnClickListener {
            presenter?.fireAdd("7")
        }

        root.findViewById<MaterialButton>(R.id.item_8).setOnClickListener {
            presenter?.fireAdd("8")
        }

        root.findViewById<MaterialButton>(R.id.item_9).setOnClickListener {
            presenter?.fireAdd("9")
        }

        root.findViewById<MaterialButton>(R.id.item_minus).setOnClickListener {
            presenter?.fireAdd("-")
        }

        root.findViewById<MaterialButton>(R.id.item_plu).setOnClickListener {
            presenter?.doAction(1)
        }

        root.findViewById<MaterialButton>(R.id.item_mnoz).setOnClickListener {
            presenter?.doAction(2)
        }

        root.findViewById<MaterialButton>(R.id.item_razd).setOnClickListener {
            presenter?.doAction(3)
        }

        root.findViewById<MaterialButton>(R.id.item_rav).setOnClickListener {
            presenter?.ravno()
        }

        return root
    }

    companion object {
        fun newInstance(): Lab3Fragment {
            return Lab3Fragment()
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    override fun displayValue(res: String) {
        resultText?.text = res
    }
}
