package dev.umerov.project.fragment.main.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem
import dev.umerov.project.settings.Settings
import dev.umerov.project.util.Utils.createPageTransform

class FinanceTabsFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_finance_tabs, container, false) as ViewGroup
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager: ViewPager2 = view.findViewById(R.id.viewpager)
        viewPager.setPageTransformer(
            createPageTransform(
                Settings.get().main().viewpager_page_transform
            )
        )
        setupViewPager(viewPager, view)
    }

    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.finance)
            actionBar.subtitle = null
        }
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    private fun setupViewPager(viewPager: ViewPager2, view: View) {
        val tabs: MutableList<ITab> = ArrayList()
        tabs.add(Tab(object : IFragmentCreator {
            override fun create(): Fragment {
                return FinanceWalletsFragment.newInstance(false)
            }
        }, getString(R.string.finance_wallets)))
        tabs.add(Tab(object : IFragmentCreator {
            override fun create(): Fragment {
                return FinanceWalletsFragment.newInstance(true)
            }
        }, getString(R.string.finance_credit_cards)))
        tabs.add(Tab(object : IFragmentCreator {
            override fun create(): Fragment {
                return BalanceFragment.newInstance()
            }
        }, getString(R.string.more_info)))
        val adapter = Adapter(tabs, this)
        viewPager.adapter = adapter
        TabLayoutMediator(
            view.findViewById(R.id.tablayout),
            viewPager
        ) { tab, position -> tab.text = tabs[position].tabTitle }.attach()
    }

    interface ITab {
        val tabTitle: String?
        val fragmentCreator: IFragmentCreator
    }

    interface IFragmentCreator {
        fun create(): Fragment
    }

    private class Tab(override val fragmentCreator: IFragmentCreator, val title: String) : ITab {
        override val tabTitle: String
            get() = title
    }

    internal class Adapter(private val tabs: List<ITab>, fragmentActivity: Fragment) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return tabs[position].fragmentCreator.create()
        }

        override fun getItemCount(): Int {
            return tabs.size
        }
    }

    companion object {
        fun newInstance(): FinanceTabsFragment {
            return FinanceTabsFragment()
        }
    }
}
