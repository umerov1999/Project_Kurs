package dev.umerov.project.activity.crash

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.slidr.Slidr
import dev.umerov.project.activity.slidr.model.SlidrConfig
import dev.umerov.project.activity.slidr.model.SlidrListener
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.util.toast.CustomToast

class DefaultErrorActivity : AppCompatActivity() {
    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.App_CrashError)
        super.onCreate(savedInstanceState)
        Slidr.attach(
            this,
            SlidrConfig.Builder().scrimColor(CurrentTheme.getColorBackground(this))
                .listener(object : SlidrListener {
                    override fun onSlideStateChanged(state: Int) {
                    }

                    override fun onSlideChange(percent: Float) {
                    }

                    override fun onSlideOpened() {
                    }

                    override fun onSlideClosed(): Boolean {
                        CrashUtils.closeApplication(this@DefaultErrorActivity)
                        return true
                    }
                }).build()
        )
        setContentView(R.layout.crash_error_activity)
        findViewById<MaterialButton>(R.id.crash_error_activity_restart_button).setOnClickListener {
            CrashUtils.restartApplication(
                this
            )
        }

        if (intent.getBooleanExtra(Extra.IS_OUT_OF_MEMORY, false)) {
            findViewById<MaterialButton>(R.id.crash_error_activity_more_info_button).visibility =
                View.GONE
            findViewById<ImageView>(R.id.crash_error_activity_bag).visibility = View.GONE
            findViewById<TextView>(R.id.crash_error_activity_throwable).setText(R.string.crash_error_activity_out_of_memory)
        }

        findViewById<MaterialButton>(R.id.crash_error_activity_more_info_button).setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.crash_error_activity_error_details_title)
                .setMessage(CrashUtils.getAllErrorDetailsFromIntent(this, intent))
                .setPositiveButton(R.string.crash_error_activity_error_details_close, null)
                .setNeutralButton(
                    R.string.crash_error_activity_error_details_copy
                ) { _, _ -> copyErrorToClipboard() }
                .show()
            val textView = dialog.findViewById<TextView>(android.R.id.message)
            textView?.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.crash_error_activity_error_details_text_size)
            )
        }
    }

    private fun copyErrorToClipboard() {
        val errorInformation = CrashUtils.getAllErrorDetailsFromIntent(this, intent)
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        if (clipboard != null) {
            val clip = ClipData.newPlainText(
                getString(R.string.crash_error_activity_error_details_clipboard_label),
                errorInformation
            )
            clipboard.setPrimaryClip(clip)
            CustomToast.createCustomToast(this, null)
                ?.showToastInfo(R.string.crash_error_activity_error_details_copied)
        }
    }
}
