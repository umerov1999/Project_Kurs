package dev.umerov.project.listener

import androidx.recyclerview.widget.RecyclerView
import dev.umerov.project.Constants
import dev.umerov.project.picasso.PicassoInstance.Companion.with
import dev.umerov.project.settings.Settings

class PicassoPauseOnScrollListener(private val tag: String) : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            with().resumeTag(tag)
        } else {
            with().pauseTag(tag)
        }
    }

    companion object {
        fun addListener(
            recyclerView: RecyclerView?,
            tag: String = Constants.PICASSO_TAG
        ) {
            if (!Settings.get().main().isInstant_photo_display) {
                recyclerView?.addOnScrollListener(PicassoPauseOnScrollListener(tag))
            }
        }
    }
}
