package dev.umerov.project.fragment.main.lab19

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.media.exo.ExoUtil

@UnstableApi
class Lab19Presenter(context: Context) : RxSupportPresenter<ILab19View>() {
    private var isFirst = true
    var mCurrentMediaPlayer: ExoPlayer = ExoPlayer.Builder(
        context, DefaultRenderersFactory(context)
    ).build()
    val source = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
        .createMediaSource(makeMediaItem("file:///android_asset/audio_test.mp3"))

    override fun onGuiCreated(viewHost: ILab19View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_19)
            isFirst = false
        }
    }

    private fun makeMediaItem(url: String?): MediaItem {
        return MediaItem.Builder().setUri(url).build()
    }

    override fun destroy() {
        super.destroy()
        mCurrentMediaPlayer.stop()
        mCurrentMediaPlayer.clearMediaItems()
    }

    init {
        mCurrentMediaPlayer.setMediaSource(source)
        mCurrentMediaPlayer.prepare()
        mCurrentMediaPlayer.setAudioAttributes(
            AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA).build(), true
        )
        ExoUtil.startPlayer(mCurrentMediaPlayer)
    }
}
