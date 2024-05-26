package dev.umerov.project.fragment.main.lab11

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.activity.FileManagerSelectActivity
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.getParcelableCompat
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.umerov.project.modalbottomsheetdialogfragment.OptionRequest
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.main.labs.Lab11Film
import dev.umerov.project.model.main.labs.Lab11Genre
import dev.umerov.project.module.ProjectNative.appContext
import dev.umerov.project.util.Utils
import dev.umerov.project.view.Lab11GenreSelect
import okio.BufferedSink
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date


class Lab11FilmsFragment : BaseMvpFragment<Lab11FilmsPresenter, ILab11FilmsView>(),
    ILab11FilmsView, Lab11FilmsAdapter.ClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: Lab11FilmsAdapter? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_11_films)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            ENTRY_FILM_RESULT, this
        ) { _: String?, result: Bundle ->
            result.getParcelableCompat<Lab11Film>(Extra.DATA)?.let { presenter?.fireStore(it) }
        }
    }

    override fun displayCreateDialog(obj: Lab11Film) {
        val dialog = EntryFilmDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, obj)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "EntryFilmDialog")
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab11FilmsPresenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab11_films, container, false)
        if (!requireArguments().getBoolean(EXTRA_HIDE_TOOLBAR, false)) {
            (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        } else {
            root.findViewById<View>(R.id.toolbar).visibility = View.GONE
        }

        mRecyclerView = root.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = layoutManager
        mAdapter = Lab11FilmsAdapter(emptyList())
        mAdapter?.setClickListener(this)
        mRecyclerView?.adapter = mAdapter
        root.findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            presenter?.fireAdd()
        }
        return root
    }

    private fun showMenu(pos: Int) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                0,
                getString(R.string.edit),
                R.drawable.pencil,
                true
            )
        )
        menus.add(
            OptionRequest(
                1,
                getString(R.string.delete),
                R.drawable.ic_outline_delete,
                true
            )
        )
        menus.show(
            childFragmentManager,
            "lab11_films_options"
        ) { _, option ->
            when (option.id) {
                0 -> presenter?.fireEdit(pos)
                1 -> presenter?.fireDelete(pos)
            }
        }
    }

    companion object {
        const val ENTRY_FILM_RESULT = "entry_film_result"
        fun newInstance(hasTab: Boolean): Lab11FilmsFragment {
            val fragment = Lab11FilmsFragment()
            val bundle = Bundle()
            bundle.putBoolean(EXTRA_HIDE_TOOLBAR, !hasTab)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun notifyItemChanged(index: Int) {
        mAdapter?.notifyItemChanged(index)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun notifyDataSetChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        mAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun notifyDataRemoved(position: Int, count: Int) {
        mAdapter?.notifyItemRangeRemoved(position, count)
    }

    override fun displayData(data: List<Lab11Film>) {
        mAdapter?.setData(data)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    class EntryFilmDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_lab11_film, null)
            val obj = requireArguments().getParcelableCompat<Lab11Film>(Extra.DATA)!!

            val mTitle = view.findViewById<TextInputEditText>(R.id.edit_title)
            val mYear = view.findViewById<TextInputEditText>(R.id.edit_year)
            val mGenre = view.findViewById<Lab11GenreSelect>(R.id.edit_genre)

            if (savedInstanceState == null) {
                mTitle.setText(obj.title)
                mGenre.setSelected(obj.genre)
                mYear.setText(obj.year.toString())
            }
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                    obj.setTitle(mTitle.text.toString())
                    try {
                        obj.setYear(mYear.text.toString().toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    obj.setGenre(mGenre.selected)
                    val res = Bundle()
                    res.putParcelable(Extra.DATA, obj)
                    parentFragmentManager.setFragmentResult(
                        ENTRY_FILM_RESULT,
                        res
                    )
                    dismiss()
                }.create()
        }
    }

    override fun onClick(position: Int, value: Lab11Film) {
        presenter?.fireClick(position)
    }

    override fun onLongClick(position: Int, value: Lab11Film): Boolean {
        showMenu(position)
        return true
    }

    override fun onDelete(position: Int, value: Lab11Film) {
        presenter?.fireDeleteAnim(position)
    }

    override fun onEdit(position: Int, title: String?, genre: Lab11Genre?, year: Int) {
        presenter?.fireEdit(position, title, genre, year)
    }

    private val DATE_FORMAT: DateFormat =
        SimpleDateFormat("yyyyMMdd_HHmmss", Utils.appLocale)

    private val selectPhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val dest = File(
                parentDir(), DATE_FORMAT.format(
                    Date()
                ) + ".jpg"
            )
            val bufferedSink: BufferedSink = dest.sink().buffer()
            bufferedSink.writeAll(File(result.data?.getStringExtra(Extra.PATH).orEmpty()).source())
            bufferedSink.close()
            presenter?.fireOnSelectImage(dest.absolutePath)
        }
    }

    private fun parentDir(): File {
        val file = File(appContext.cacheDir, "db_images")
        if (file.isFile) {
            file.delete()
        }
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    override fun onSelectPicture(position: Int, value: Lab11Film) {
        presenter?.fireSelectImage(position)
        selectPhoto.launch(
            FileManagerSelectActivity.makeFileManager(
                requireActivity(),
                Environment.getExternalStorageDirectory().absolutePath,
                "jpg"
            )
        )
    }
}
