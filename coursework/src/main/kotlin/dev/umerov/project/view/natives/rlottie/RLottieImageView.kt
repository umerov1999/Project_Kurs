package dev.umerov.project.view.natives.rlottie

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.RawRes
import androidx.appcompat.widget.AppCompatImageView
import dev.umerov.project.R
import dev.umerov.project.module.BufferWriteNative
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.module.rlottie.RLottieDrawable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File

class RLottieImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {
    private var layerColors: HashMap<String, Int>? = null
    private var animatedDrawable: RLottieDrawable? = null
    private var autoRepeat: Boolean
    private var attachedToWindow = false
    private var playing = false
    private var mDisposable: Disposable? = null
    fun clearLayerColors() {
        layerColors?.clear()
    }

    fun setLayerColor(layer: String, color: Int) {
        if (layerColors == null) {
            layerColors = HashMap()
        }
        (layerColors ?: return)[layer] = color
        animatedDrawable?.setLayerColor(layer, color)
    }

    fun replaceColors(colors: IntArray?) {
        animatedDrawable?.replaceColors(colors)
    }

    private fun setAnimation(rLottieDrawable: RLottieDrawable) {
        animatedDrawable = rLottieDrawable
        animatedDrawable?.setAutoRepeat(if (autoRepeat) 1 else 0)
        if (layerColors != null) {
            animatedDrawable?.beginApplyLayerColors()
            for ((key, value) in layerColors ?: return) {
                animatedDrawable?.setLayerColor(key, value)
            }
            animatedDrawable?.commitApplyLayerColors()
        }
        animatedDrawable?.setAllowDecodeSingleFrame(true)
        animatedDrawable?.setCurrentParentView(this)
        setImageDrawable(animatedDrawable)
    }

    @JvmOverloads
    fun fromRes(
        @RawRes resId: Int,
        w: Int,
        h: Int,
        colorReplacement: IntArray? = null,
        useMoveColor: Boolean = false
    ) {
        if (!ProjectNative.isNativeLoaded) {
            return
        }
        clearAnimationDrawable()
        setAnimation(
            RLottieDrawable(
                resId,
                w,
                h,
                false,
                colorReplacement,
                useMoveColor
            )
        )
    }

    fun fromFile(file: File, w: Int, h: Int) {
        if (!ProjectNative.isNativeLoaded) {
            return
        }
        clearAnimationDrawable()
        setAnimation(
            RLottieDrawable(
                file, false, w, h,
                limitFps = false,
                colorReplacement = null,
                useMoveColor = false
            )
        )
    }

    fun fromString(jsonString: BufferWriteNative, w: Int, h: Int) {
        if (!ProjectNative.isNativeLoaded) {
            return
        }
        clearAnimationDrawable()
        setAnimation(
            RLottieDrawable(
                jsonString, w, h,
                limitFps = false,
                colorReplacement = null,
                useMoveColor = false
            )
        )
    }

    fun clearAnimationDrawable() {
        mDisposable?.dispose()
        animatedDrawable?.let {
            it.stop()
            it.callback = null
            it.recycle()
            animatedDrawable = null
        }
        setImageDrawable(null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachedToWindow = true
        animatedDrawable?.setCurrentParentView(this)
        if (playing) {
            animatedDrawable?.start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mDisposable?.dispose()
        attachedToWindow = false
        animatedDrawable?.stop()
        animatedDrawable?.setCurrentParentView(null)
    }

    fun isPlaying(): Boolean {
        return animatedDrawable != null && animatedDrawable?.isRunning == true
    }

    fun setAutoRepeat(repeat: Boolean) {
        autoRepeat = repeat
    }

    fun setProgress(progress: Float) {
        animatedDrawable?.setProgress(progress)
    }

    override fun setImageDrawable(dr: Drawable?) {
        super.setImageDrawable(dr)
        if (dr !is RLottieDrawable) {
            mDisposable?.dispose()
            animatedDrawable?.let {
                it.stop()
                it.callback = null
                it.recycle()
                animatedDrawable = null
            }
        }
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        mDisposable?.dispose()
        animatedDrawable?.let {
            it.stop()
            it.callback = null
            it.recycle()
            animatedDrawable = null
        }
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        mDisposable?.dispose()
        animatedDrawable?.let {
            it.stop()
            it.callback = null
            it.recycle()
            animatedDrawable = null
        }
    }

    fun playAnimation() {
        playing = true
        if (attachedToWindow) {
            animatedDrawable?.start()
        }
    }

    fun replayAnimation() {
        if (animatedDrawable == null) {
            return
        }
        playing = true
        if (attachedToWindow) {
            animatedDrawable?.stop()
            animatedDrawable?.setAutoRepeat(1)
            animatedDrawable?.start()
        }
    }

    fun resetFrame() {
        playing = true
        if (attachedToWindow) {
            animatedDrawable?.setAutoRepeat(1)
        }
    }

    fun stopAnimation() {
        playing = false
        if (attachedToWindow) {
            animatedDrawable?.stop()
        }
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RLottieImageView)
        val animRes = a.getResourceId(R.styleable.RLottieImageView_fromRes, 0)
        autoRepeat = a.getBoolean(R.styleable.RLottieImageView_loopAnimation, false)
        val width = a.getDimension(R.styleable.RLottieImageView_w, 28f).toInt()
        val height = a.getDimension(R.styleable.RLottieImageView_h, 28f).toInt()
        a.recycle()
        if (ProjectNative.isNativeLoaded && animRes != 0) {
            animatedDrawable =
                RLottieDrawable(animRes, width, height, false, null, false)
            setAnimation(animatedDrawable!!)
            playAnimation()
        }
    }
}
