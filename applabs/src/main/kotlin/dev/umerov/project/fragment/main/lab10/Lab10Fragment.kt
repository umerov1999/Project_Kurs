package dev.umerov.project.fragment.main.lab10

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso3.Picasso
import dev.umerov.project.Constants
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.activity.FileManagerSelectActivity
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem
import dev.umerov.project.picasso.PicassoInstance
import dev.umerov.project.util.toast.CustomToast

class Lab10Fragment : BaseMvpFragment<Lab10Presenter, ILab10View>(),
    ILab10View {
    private var photo: ShapeableImageView? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_10)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab10Presenter()

    private val selectPhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                photo?.let {
                    PicassoInstance.with()
                        .load("thumb_file://${result.data?.getStringExtra(Extra.PATH)}")
                        .tag(Constants.PICASSO_TAG)
                        .priority(Picasso.Priority.LOW)
                        .into(it)
                }
            } catch (e: Exception) {
                CustomToast.createCustomToast(requireActivity(), view)?.showToastThrowable(e)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab10, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        root.findViewById<MaterialButton>(R.id.select).setOnClickListener {
            selectPhoto.launch(
                FileManagerSelectActivity.makeFileManager(
                    requireActivity(),
                    Environment.getExternalStorageDirectory().absolutePath,
                    "jpg"
                )
            )
        }
        photo = root.findViewById(R.id.item_image)
        return root
    }

    companion object {
        fun newInstance(): Lab10Fragment {
            return Lab10Fragment()
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }
}
