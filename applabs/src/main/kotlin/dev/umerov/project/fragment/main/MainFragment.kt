package dev.umerov.project.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.main.MainButton
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.orZero
import dev.umerov.project.place.Place
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.util.Utils
import dev.umerov.project.view.natives.rlottie.RLottieImageView

class MainFragment : BaseMvpFragment<MainPresenter, IMainView>(),
    IMainView, MainButtonsAdapter.ClickListener {
    private var mAdapter: MainButtonsAdapter? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
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

    override fun getPresenterFactory(saveInstanceState: Bundle?) = MainPresenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)
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

        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        mAdapter = MainButtonsAdapter(emptyList())
        mAdapter?.setClickListener(this)
        recyclerView.adapter = mAdapter

        val Goto: FloatingActionButton = root.findViewById(R.id.goto_button)
        Goto.setOnClickListener {
            var pos = mAdapter?.itemCount.orZero()
            if (pos > 0) {
                pos--
            }
            recyclerView.stopScroll()
            recyclerView.scrollToPosition(pos)
        }
        return root
    }

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }

    override fun displayData(data: Array<MainButton>) {
        mAdapter?.setData(data)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_LONG)?.showToast(res)
    }

    override fun openPlace(place: Place) {
        place.tryOpenWith(requireActivity())
    }

    override fun onClick(value: MainButton) {
        presenter?.fireClickedButton(value)
    }
}
