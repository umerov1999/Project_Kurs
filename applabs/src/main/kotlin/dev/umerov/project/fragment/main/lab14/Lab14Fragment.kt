package dev.umerov.project.fragment.main.lab14

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
import com.google.android.material.navigation.NavigationView
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
import dev.umerov.project.model.main.labs.Lab14AudioAlbum
import dev.umerov.project.module.ProjectNative.appContext
import dev.umerov.project.util.Utils
import okio.BufferedSink
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date


class Lab14Fragment : BaseMvpFragment<Lab14Presenter, ILab14View>(),
    ILab14View, Lab14Adapter.ClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: Lab14Adapter? = null
    private var mNavigation: NavigationView? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_14)
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
            ENTRY_PLAYLIST_RESULT, this
        ) { _: String?, result: Bundle ->
            result.getParcelableCompat<Lab14AudioAlbum>(Extra.DATA)
                ?.let { presenter?.fireStore(it) }
        }
    }

    override fun displayCreateDialog(obj: Lab14AudioAlbum) {
        val dialog = EntryPlaylistDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, obj)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "EntryPlaylistDialog")
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab14Presenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab14, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        mRecyclerView = root.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = layoutManager
        mAdapter = Lab14Adapter(emptyList())
        mAdapter?.setClickListener(this)
        mRecyclerView?.adapter = mAdapter
        root.findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            presenter?.fireAdd()
        }
        mNavigation = root.findViewById(R.id.music_option)
        mNavigation?.setNavigationItemSelectedListener {
            presenter?.fireChangeSelectItem(it.itemId)
            true
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
            "lab14_options"
        ) { _, option ->
            when (option.id) {
                0 -> presenter?.fireEdit(pos)
                1 -> presenter?.fireDelete(pos)
            }
        }
    }

    companion object {
        const val ENTRY_PLAYLIST_RESULT = "entry_playlist_result"
        fun newInstance(): Lab14Fragment {
            return Lab14Fragment()
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

    override fun updateCheckedItem(checkedItem: Int) {
        mNavigation?.setCheckedItem(checkedItem)
    }

    override fun displayData(data: List<Lab14AudioAlbum>) {
        mAdapter?.setData(data)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    class EntryPlaylistDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_lab14_playlist, null)
            val obj = requireArguments().getParcelableCompat<Lab14AudioAlbum>(Extra.DATA)!!

            val mTitle = view.findViewById<TextInputEditText>(R.id.edit_title)
            val mYear = view.findViewById<TextInputEditText>(R.id.edit_year)
            val mArtist = view.findViewById<TextInputEditText>(R.id.edit_artist)

            if (savedInstanceState == null) {
                mTitle.setText(obj.title)
                mArtist.setText(obj.artist)
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
                    obj.setArtist(mArtist.text.toString())
                    val res = Bundle()
                    res.putParcelable(Extra.DATA, obj)
                    parentFragmentManager.setFragmentResult(
                        ENTRY_PLAYLIST_RESULT,
                        res
                    )
                    dismiss()
                }.create()
        }
    }

    override fun onClick(position: Int, value: Lab14AudioAlbum) {
        presenter?.fireClick(position)
    }

    override fun onLongClick(position: Int, value: Lab14AudioAlbum): Boolean {
        showMenu(position)
        return true
    }

    override fun onDelete(position: Int, value: Lab14AudioAlbum) {
        presenter?.fireDeleteAnim(position)
    }

    override fun onEdit(position: Int, title: String?, artist: String?, year: Int) {
        presenter?.fireEdit(position, title, artist, year)
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

    override fun onSelectPicture(position: Int, value: Lab14AudioAlbum) {
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
