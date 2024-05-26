package dev.umerov.project.activity.lab13

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.BaseMvpActivity
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.util.Utils
import dev.umerov.project.view.natives.rlottie.RLottieImageView

class Lab13Activity : BaseMvpActivity<Lab13Presenter, ILab13View>(),
    ILab13View {
    private var anim: RLottieImageView? = null
    private var isRight = true
    override fun onResume() {
        super.onResume()
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(this, true)
            .build()
            .apply(this)
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab13Presenter()

    @get:LayoutRes
    override val noMainContentView: Int
        get() = R.layout.fragment_lab13

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        anim = findViewById(R.id.lottie_animation)
        if (ProjectNative.isNativeLoaded) {
            anim?.fromRes(
                R.raw.lab13_run_right,
                Utils.dp(400f),
                Utils.dp(180f),
                null
            )
        }
        val view: View = findViewById(R.id.view)
        view.setOnTouchListener { _, event ->
            return@setOnTouchListener when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val newIsRight = event.x / view.width.toDouble() >= 0.5
                    if (newIsRight != isRight) {
                        isRight = newIsRight
                        if (isRight) {
                            if (ProjectNative.isNativeLoaded) {
                                anim?.fromRes(
                                    R.raw.lab13_run_right,
                                    Utils.dp(400f),
                                    Utils.dp(180f),
                                    null
                                )
                            }
                        } else {
                            if (ProjectNative.isNativeLoaded) {
                                anim?.fromRes(
                                    R.raw.lab13_run_left,
                                    Utils.dp(400f),
                                    Utils.dp(180f),
                                    null
                                )
                            }
                        }
                        anim?.playAnimation()
                    }
                    true
                }

                MotionEvent.ACTION_DOWN -> {
                    val newIsRight = event.x / view.width.toDouble() >= 0.5
                    if (newIsRight != isRight) {
                        isRight = newIsRight
                        if (isRight) {
                            if (ProjectNative.isNativeLoaded) {
                                anim?.fromRes(
                                    R.raw.lab13_run_right,
                                    Utils.dp(400f),
                                    Utils.dp(180f),
                                    null
                                )
                            }
                        } else {
                            if (ProjectNative.isNativeLoaded) {
                                anim?.fromRes(
                                    R.raw.lab13_run_left,
                                    Utils.dp(400f),
                                    Utils.dp(180f),
                                    null
                                )
                            }
                        }
                    }
                    anim?.playAnimation()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    anim?.stopAnimation()
                    true
                }

                else -> true
            }
        }
    }

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, Lab13Activity::class.java)
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }
}
