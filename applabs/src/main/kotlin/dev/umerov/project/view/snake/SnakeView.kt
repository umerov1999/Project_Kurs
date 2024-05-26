package dev.umerov.project.view.snake

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import dev.umerov.project.R
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.settings.Settings
import kotlin.random.Random

class SnakeView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {
    var delegate: Delegate? = null
    private var prevTime: Long = 0

    val cellSide: Int = 65
    var widthView: Int = 0
    var heightView: Int = 0
    private val bg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val apple = Paint(Paint.ANTI_ALIAS_FLAG)
    private val snake = Paint(Paint.ANTI_ALIAS_FLAG)
    private val font = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        holder.addCallback(this)
        apple.color = CurrentTheme.getColorTertiary(context)
        bg.color = CurrentTheme.getColorSurface(context)
        font.color = Color.RED
        snake.color = CurrentTheme.getColorOnSurface(context)
    }

    fun isNeedChange(): Boolean {
        val now = System.currentTimeMillis()
        val elapsedTime = now - prevTime
        if (elapsedTime > Settings.get().main().snakeRefreshMS) {
            prevTime = now
            return true
        } else {
            return false
        }
    }

    fun draw() {
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas(null)
            synchronized(holder) {
                widthView = width
                heightView = height

                canvas.drawRect(0f, 0f, widthView.toFloat(), heightView.toFloat(), bg)

                delegate?.snake?.let {
                    canvas.drawOval(
                        it.apple.x.toFloat(),
                        it.apple.y.toFloat(),
                        it.apple.x.toFloat() + cellSide - 15,
                        it.apple.y.toFloat() + cellSide - 15,
                        apple
                    )
                    for (piece in it.segments) {
                        canvas.drawRect(
                            piece.x.toFloat(),
                            piece.y.toFloat(),
                            (piece.x + it.cellSide).toFloat(),
                            (piece.y + it.cellSide).toFloat(),
                            snake
                        )
                    }
                    if (it.isGameOver) {
                        font.textSize = 100f
                        canvas.drawText(
                            context.getString(R.string.game_over),
                            widthView - (widthView * 0.79f),
                            widthView - (widthView * 1.25f) / 2,
                            font
                        )
                    }
                }
            }
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    val isSnakeBite: Boolean
        get() {
            delegate?.snake?.let {
                val pos = it.segments[0]
                val distX = pos.x.coerceAtLeast(it.apple.x) - pos.x.coerceAtMost(it.apple.x)
                val distY = pos.y.coerceAtLeast(it.apple.y) - pos.y.coerceAtMost(it.apple.y)
                return distX <= 40 && distY <= 40
            }
            return false
        }

    val isDistance: Boolean
        get() {
            delegate?.snake?.let {
                val pos = it.segments[0]
                return (pos.x < 0 || pos.x > widthView - (2 * it.cellSide) / 2 || pos.y < 0) || pos.y > heightView - (2 * it.cellSide) / 2
            }
            return false
        }

    fun collision(): Boolean {
        delegate?.snake?.let {
            val size = it.segments.size
            val pos = it.segments[0]
            for (i in 2..<size) {
                val p = it.segments[i]
                val distX = pos.x.coerceAtLeast(p.x) - pos.x.coerceAtMost(p.x)
                val distY = pos.y.coerceAtLeast(p.y) - pos.y.coerceAtMost(p.y)
                if (distX <= 30 && distY <= 30) {
                    return true
                }
            }
        }

        return false
    }

    fun refresh() {
        val availWidth = widthView - cellSide
        val availHeight = heightView - cellSide

        val col = Random.nextInt(availWidth / cellSide)
        val row = Random.nextInt(availHeight / cellSide)

        var newX = col * cellSide
        var newY = row * cellSide

        delegate?.snake?.let {
            if (newX == it.apple.x) newX = Random.nextInt(availWidth / cellSide) * cellSide
            else if (newY == it.apple.y) newY = Random.nextInt(availHeight / cellSide) * cellSide

            it.apple.x = newX
            it.apple.y = newY

            for (piece in it.segments) {
                if (piece.x / it.cellSide == col && piece.y / it.cellSide == row) {
                    refresh()
                    break
                }
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        prevTime = 0
        delegate?.onSurfaceCreated(format, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }
}
