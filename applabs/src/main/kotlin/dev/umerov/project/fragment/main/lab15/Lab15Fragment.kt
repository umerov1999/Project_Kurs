package dev.umerov.project.fragment.main.lab15

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem

class Lab15Fragment : BaseMvpFragment<Lab15Presenter, ILab15View>(),
    ILab15View {
    private var text: MaterialTextView? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_15)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab15Presenter(requireActivity())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab15, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        text = root.findViewById(R.id.item_text)

        root.findViewById<MaterialButton>(R.id.start).setOnClickListener {
            presenter?.fireStart()
        }

        root.findViewById<MaterialButton>(R.id.pause).setOnClickListener {
            presenter?.firePause()
        }

        root.findViewById<MaterialButton>(R.id.terminate).setOnClickListener {
            presenter?.fireInterrupt()
        }

        root.findViewById<MaterialButton>(R.id.resume).setOnClickListener {
            presenter?.fireResume()
        }
        return root
    }

    companion object {
        fun newInstance(): Lab15Fragment {
            return Lab15Fragment()
        }
    }

    override fun showMessage(str: String?) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(str)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    override fun printText(str: String?) {
        text?.text = str
    }
}
