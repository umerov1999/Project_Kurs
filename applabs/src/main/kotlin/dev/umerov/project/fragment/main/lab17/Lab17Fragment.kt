package dev.umerov.project.fragment.main.lab17

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem

class Lab17Fragment : BaseMvpFragment<Lab17Presenter, ILab17View>(),
    ILab17View {
    private var answer: MaterialTextView? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            if (requireArguments().getBoolean(Extra.IS_POST, false)) {
                actionBar.setTitle(R.string.lab_18)
            } else {
                actionBar.setTitle(R.string.lab_17)
            }
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab17Presenter(
        requireArguments().getBoolean(
            Extra.IS_POST, false
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab17, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        answer = root.findViewById(R.id.item_answer)
        root.findViewById<MaterialButton>(R.id.button_submit).setOnClickListener {
            presenter?.fireSubmit(
                root.findViewById<TextInputEditText>(R.id.edit_url).text.toString(),
                root.findViewById<TextInputEditText>(R.id.edit_firstname).text.toString(),
                root.findViewById<TextInputEditText>(R.id.edit_lastname).text.toString()
            )
        }
        return root
    }

    companion object {
        fun newInstance(isPost: Boolean): Lab17Fragment {
            val arg = Bundle()
            arg.putBoolean(Extra.IS_POST, isPost)
            val fragment = Lab17Fragment()
            fragment.arguments = arg
            return fragment
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    override fun displayAnswer(text: String?) {
        answer?.text = text
    }
}
