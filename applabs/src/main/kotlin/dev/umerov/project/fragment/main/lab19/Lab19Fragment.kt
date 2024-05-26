package dev.umerov.project.fragment.main.lab19

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem
import dev.umerov.project.orZero
import dev.umerov.project.settings.Settings
import dev.umerov.project.view.VolumeView

class Lab19Fragment : BaseMvpFragment<Lab19Presenter, ILab19View>(),
    ILab19View {
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_19)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    @OptIn(UnstableApi::class)
    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab19Presenter(requireActivity())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab19, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        val volumeView: VolumeView = root.findViewById(R.id.volume_control)
        volumeView.setListener(object : VolumeView.OnActionListener {
            override fun onChangeVolume(value: Float) {
                Settings.get().main().volumeValue = value

                val audio =
                    requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                val maxVol = audio?.getStreamMaxVolume(AudioManager.STREAM_MUSIC).orZero()
                audio?.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    ((value / 10) * maxVol).toInt(),
                    0
                )
            }
        })
        volumeView.setValue(Settings.get().main().volumeValue, true)
        return root
    }

    companion object {
        fun newInstance(): Lab19Fragment {
            return Lab19Fragment()
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }
}
