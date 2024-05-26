package dev.umerov.project.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import dev.umerov.project.R
import dev.umerov.project.getParcelableCompat
import dev.umerov.project.util.Utils
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class VolumeView : View {
    private val colorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dotNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val radius: Long = 300
    private var total_angle = 0f
    private var angle = 0f
    private var center_x = 0f
    private var center_y = 0f
    private var x0 = 0f
    private var y0 = 0f
    private var listener: OnActionListener? = null
    var value: Float = 0f
        private set

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setWillNotDraw(false)
        initAttributes(attrs)
    }

    fun setListener(listener: OnActionListener?) {
        this.listener = listener
    }

    private fun initAttributes(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(
                attrs,
                R.styleable.VolumeView
            )

            try {
                colorPaint.color = a.getColor(R.styleable.VolumeView_controlColor, Color.GRAY)
                pointerPaint.color = a.getColor(R.styleable.VolumeView_pointerColor, Color.BLACK)
                dotNumberPaint.color =
                    a.getColor(R.styleable.VolumeView_numberAndDotColor, Color.BLUE)
                valuePaint.color = a.getColor(R.styleable.VolumeView_ValueColor, Color.BLACK)
            } finally {
                a.recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val desiredWidth = 100
        val desiredHeight = 100

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }

            MeasureSpec.AT_MOST -> {
                min(desiredWidth.toDouble(), widthSize.toDouble()).toInt()
            }

            else -> {
                desiredWidth
            }
        }
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }

            MeasureSpec.AT_MOST -> {
                min(desiredHeight.toDouble(), heightSize.toDouble()).toInt()
            }

            else -> {
                desiredHeight
            }
        }
        setMeasuredDimension(width, height)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionMasked = event.actionMasked
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            x0 = event.x
            y0 = event.y
            return true
        } else if (actionMasked == MotionEvent.ACTION_MOVE) {
            var x = event.x - center_x
            var y = center_y - event.y

            if ((x != 0f) && (y != 0f)) {
                val angleB = computeAngle(x, y)

                x = x0 - center_x
                y = center_y - y0
                val angleA = computeAngle(x, y)

                angle = (angleA - angleB).toFloat()
                calculateValue()
                listener?.onChangeVolume(value)
                invalidate()
                return true
            }
        } else if ((actionMasked == MotionEvent.ACTION_UP) || (actionMasked == MotionEvent.ACTION_CANCEL)) {
            total_angle += angle
            angle = 0f
            while (total_angle > 360f && total_angle >= 0f) {
                total_angle -= 360f
            }
            while (total_angle > -360f && total_angle < 0f) {
                total_angle += 360f
            }
            calculateValue()
            listener?.onChangeVolume(value)
            invalidate()
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.center_x = width.toFloat() / 2
        this.center_y = height.toFloat() / 2
        drawCircle(canvas)
        drawLine(canvas)
        drawNumbersAndDots(canvas)
        displayValue(canvas)
    }

    private fun drawNumbersAndDots(canvas: Canvas) {
        var dAlfa = 360f / 10
        var alfa = 0f
        dotNumberPaint.isDither = true
        dotNumberPaint.textSize = 90f
        for (i in 0..9) {
            canvas.drawText(
                i.toString(),
                (center_x + (radius + 150) * cos(Math.toRadians(-alfa.toDouble()))).toFloat(),
                (center_y - (radius + 150) * sin(
                    Math.toRadians(-alfa.toDouble())
                )).toFloat(),
                dotNumberPaint
            )
            alfa += dAlfa
        }
        dAlfa = 360f / 20
        alfa = 0f
        for (i in 0..19) {
            canvas.drawCircle(
                (center_x - (radius + 50) * cos(Math.toRadians(alfa.toDouble()))).toFloat(),
                (center_y - (radius + 50) * sin(
                    Math.toRadians(alfa.toDouble())
                )).toFloat(),
                5f,
                dotNumberPaint
            )
            alfa += dAlfa
        }
    }

    private fun drawCircle(canvas: Canvas) {
        colorPaint.isDither = true
        canvas.drawCircle(center_x, center_y, radius.toFloat(), colorPaint)
    }

    private fun drawLine(canvas: Canvas) {
        val tangle = if (angle == 0f) {
            total_angle
        } else {
            total_angle + angle
        }
        pointerPaint.isDither = true
        pointerPaint.strokeWidth = 15f
        canvas.drawLine(
            center_x,
            center_y,
            (center_x + radius * cos(Math.toRadians(tangle.toDouble()))).toFloat(),
            (center_y + radius * sin(
                Math.toRadians(tangle.toDouble())
            )).toFloat(),
            pointerPaint
        )
    }

    private fun displayValue(canvas: Canvas) {
        valuePaint.isDither = true
        valuePaint.textSize = 90f
        canvas.drawText(
            context.getString(
                R.string.volume,
                String.format(Utils.appLocale, "%.2f", value)
            ), center_x - 300, center_y + 700, valuePaint
        )
    }

    fun setValue(value: Float, notify: Boolean) {
        this.value = value
        angle = 0f
        total_angle = value * 36
        if (notify) {
            listener?.onChangeVolume(value)
        }
        invalidate()
    }

    private fun calculateValue() {
        val tangle = if (angle == 0f) {
            total_angle
        } else {
            total_angle + angle
        }
        value = tangle / 36
        if (value < 0) {
            value = 0f
        } else if (value > 10) {
            value = 10f
        }
    }

    private fun computeAngle(x: Float, y: Float): Double {
        val RADS_TO_DEGREES = 360 / (Math.PI * 2)
        var result = atan2(y.toDouble(), x.toDouble()) * RADS_TO_DEGREES

        if (result < 0) {
            result += 360
        }

        return result
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val state = Bundle()
        state.putParcelable("PARENT", superState)
        state.putFloat("value", value)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as Bundle
        val superState = savedState.getParcelableCompat<Parcelable>("PARENT")
        super.onRestoreInstanceState(superState)
        setValue(savedState.getFloat("value"), false)
    }

    interface OnActionListener {
        fun onChangeVolume(value: Float)
    }
}
