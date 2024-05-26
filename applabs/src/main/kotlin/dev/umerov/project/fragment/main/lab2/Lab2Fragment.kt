package dev.umerov.project.fragment.main.lab2

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
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

class Lab2Fragment : BaseMvpFragment<Lab2Presenter, ILab2View>(),
    ILab2View {
    private var anim: RLottieImageView? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_2)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab2Presenter()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab2, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        savedInstanceState ?: {
            presenter?.selectBaseColor(CurrentTheme.getColorOnSurface(requireActivity()))
        }
        anim = root.findViewById(R.id.lottie_animation)
        if (ProjectNative.isNativeLoaded) {
            anim?.fromRes(
                R.raw.project,
                Utils.dp(400f),
                Utils.dp(400f),
                intArrayOf(
                    0x000000,
                    CurrentTheme.getColorOnSurface(requireActivity()),
                    0xffffff,
                    CurrentTheme.getColorOnSurface(requireActivity())
                )
            )
            anim?.playAnimation()
        }
        root.setOnTouchListener { _, event ->
            return@setOnTouchListener when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    presenter?.fireRegisterSnap(
                        event.x,
                        event.y,
                        root.width.toFloat(),
                        root.height.toFloat()
                    )
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    presenter?.firePickColor(event.x, event.y)
                    true
                }

                else -> true
            }
        }
        return root
    }

    companion object {
        fun newInstance(): Lab2Fragment {
            return Lab2Fragment()
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    override fun updateColor(color: Int) {
        if (ProjectNative.isNativeLoaded) {
            anim?.replaceColors(
                intArrayOf(
                    0x000000,
                    color,
                    0xffffff,
                    color
                )
            )
        }
    }
}
