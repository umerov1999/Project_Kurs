package dev.umerov.project.fragment.main.lab16

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem

class Lab16Fragment : BaseMvpFragment<Lab16Presenter, ILab16View>(),
    ILab16View {
    private lateinit var root: View
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

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab16Presenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_lab16, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

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
        fun newInstance(): Lab16Fragment {
            return Lab16Fragment()
        }
    }

    override fun showMessage(str: String?) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(str)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    override fun setSize(rId: Int, width: Int, height: Int) {
        root.findViewById<MaterialButton>(rId)?.let {
            it.width = width
            it.height = height
        }
    }
}
