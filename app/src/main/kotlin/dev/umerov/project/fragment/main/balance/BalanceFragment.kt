package dev.umerov.project.fragment.main.balance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.db.Register
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.util.Utils
import dev.umerov.project.view.natives.rlottie.RLottieImageView

class BalanceFragment : BaseMvpFragment<BalancePresenter, IBalanceView>(),
    IBalanceView {
    private var balance: MaterialTextView? = null
    private var taked: MaterialTextView? = null
    private var pasted: MaterialTextView? = null
    private var operationsCount: MaterialTextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_balance, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

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

        balance = root.findViewById(R.id.balance)
        taked = root.findViewById(R.id.taked)
        pasted = root.findViewById(R.id.pasted)
        operationsCount = root.findViewById(R.id.operations_count)
        return root
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        BalancePresenter(saveInstanceState)

    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.BALANCE)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    companion object {
        fun newInstance(): BalanceFragment {
            return BalanceFragment()
        }
    }

    override fun displayData(register: Register?) {
        balance?.text = requireActivity().getString(
            R.string.rub,
            String.format("%.2f", register?.coinBalance)
        )
        taked?.text = requireActivity().getString(
            R.string.rub,
            String.format("%.2f", register?.coinTaked)
        )
        pasted?.text = requireActivity().getString(
            R.string.rub,
            String.format("%.2f", register?.coinPasted)
        )
        operationsCount?.text = register?.operationsCount.toString()
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_LONG)?.showToast(res)
    }
}
