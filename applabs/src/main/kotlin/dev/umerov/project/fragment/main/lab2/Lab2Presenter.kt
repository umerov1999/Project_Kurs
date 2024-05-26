package dev.umerov.project.fragment.main.lab2

import android.graphics.Color
import androidx.annotation.ColorInt
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import kotlin.math.atan2

class Lab2Presenter : RxSupportPresenter<ILab2View>() {
    private var isFirst = true

    @ColorInt
    private var color: Int = 0x333333
    private var x: Float = 0f
    private var y: Float = 0f
    private var width: Float = 0f
    private var height: Float = 0f

    fun fireRegisterSnap(x: Float, y: Float, width: Float, height: Float) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    private fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val rad = atan2((y1 - y2).toDouble(), (x2 - x1).toDouble()) + Math.PI
        return (rad * 180 / Math.PI + 180) % 360
    }

    private fun inRange(angle: Double, init: Float, end: Float): Boolean {
        return angle >= init && angle < end
    }

    fun firePickColor(x: Float, y: Float) {
        val angle = getAngle(this.x, this.y, x, y)

        val colorTmp = if (inRange(angle, 0f, 20f) || inRange(angle, 160f, 180f) || inRange(
                angle,
                340f,
                360f
            )
        ) {
            Color.GREEN
        } else if (inRange(angle, 75f, 100f) || inRange(angle, 250f, 285f)) {
            Color.YELLOW
        } else {
            Color.RED
        }

        if (color != colorTmp) {
            color = lerpColor(color, colorTmp, 0.07)
            view?.updateColor(color)
        }
    }

    override fun onGuiCreated(viewHost: ILab2View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_2)
            isFirst = false
        }
        viewHost.updateColor(color)
    }

    fun selectBaseColor(@ColorInt color: Int) {
        this.color = color
    }

    @ColorInt
    private fun lerpColor(@ColorInt a: Int, @ColorInt b: Int, percent: Double): Int {
        val red: Int = lerp(Color.red(a), Color.red(b), percent)
        val blue: Int = lerp(Color.blue(a), Color.blue(b), percent)
        val green: Int = lerp(Color.green(a), Color.green(b), percent)
        val alpha: Int = lerp(Color.alpha(a), Color.alpha(b), percent)
        return Color.argb(alpha, red, green, blue)
    }

    private fun lerp(a: Int, b: Int, percent: Double): Int {
        return ((1 - percent) * a + percent * b).toInt()
    }
}