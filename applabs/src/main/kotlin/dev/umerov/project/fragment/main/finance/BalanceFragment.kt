package dev.umerov.project.fragment.main.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.main.labs.FinanceBalance
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.util.Utils
import dev.umerov.project.util.ViewUtils
import dev.umerov.project.view.natives.rlottie.RLottieImageView

class BalanceFragment : BaseMvpFragment<BalancePresenter, IBalanceView>(),
    IBalanceView {
    private var fullBalance: MaterialTextView? = null
    private var walletsBalance: MaterialTextView? = null
    private var creditBalance: MaterialTextView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_balance, container, false)

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout?.setOnRefreshListener {
            mSwipeRefreshLayout?.isRefreshing = false
            presenter?.fetch()
        }
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)

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

        fullBalance = root.findViewById(R.id.full_balance)
        walletsBalance = root.findViewById(R.id.wallet_balance)
        creditBalance = root.findViewById(R.id.credit_balance)
        return root
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = BalancePresenter()

    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
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

    override fun displayData(financeBalance: FinanceBalance?) {
        if (financeBalance == null) {
            fullBalance?.text = null
            walletsBalance?.text = null
            creditBalance?.text = null
        } else {
            fullBalance?.text = requireActivity().getString(
                R.string.full_balance,
                String.format(Utils.appLocale, "%.2f", financeBalance.fullBalance)
            )
            walletsBalance?.text = requireActivity().getString(
                R.string.wallets_balance,
                String.format(Utils.appLocale, "%.2f", financeBalance.walletBalance)
            )
            creditBalance?.text = requireActivity().getString(
                R.string.credit_balance,
                String.format(Utils.appLocale, "%.2f", financeBalance.creditBalance)
            )
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_LONG)?.showToast(res)
    }
}
