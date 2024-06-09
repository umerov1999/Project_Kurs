package dev.umerov.project.fragment.filemanager

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.StubAnimatorListener
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils
import dev.umerov.project.activity.EnterPinActivity
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.fragment.filemanager.FileManagerAdapter.ClickListener
import dev.umerov.project.fromIOToMain
import dev.umerov.project.getParcelableCompat
import dev.umerov.project.getParcelableExtraCompat
import dev.umerov.project.listener.BackPressCallback
import dev.umerov.project.listener.CanBackPressedCallback
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.listener.PicassoPauseOnScrollListener
import dev.umerov.project.listener.UpdatableNavigation
import dev.umerov.project.model.FileItem
import dev.umerov.project.model.FileType
import dev.umerov.project.model.SectionItem
import dev.umerov.project.place.PlaceFactory
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.settings.Settings
import dev.umerov.project.util.Utils
import dev.umerov.project.util.ViewUtils
import dev.umerov.project.util.rxutils.RxUtils
import dev.umerov.project.view.MySearchView
import dev.umerov.project.view.natives.rlottie.RLottieImageView
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FileManagerFragment : BaseMvpFragment<FileManagerPresenter, IFileManagerView>(),
    IFileManagerView, ClickListener, BackPressCallback, CanBackPressedCallback {
    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: GridLayoutManager? = null
    private var empty: TextView? = null
    private var loading: RLottieImageView? = null
    private var tvCurrentDir: TextView? = null
    private var mAdapter: FileManagerAdapter? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private var animationDispose = Disposable.disposed()
    private var mAnimationLoaded = false
    private var animLoad: ObjectAnimator? = null
    private var mySearchView: MySearchView? = null
    private var addButton: FloatingActionButton? = null

    override fun onDestroy() {
        super.onDestroy()
        animationDispose.dispose()
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = ActivityUtils.supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.file_manager)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = FileManagerPresenter(
        File(requireArguments().getString(Extra.PATH)!!),
        requireArguments().getBoolean(Extra.POSITION)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_file_explorer, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        mRecyclerView = root.findViewById(R.id.list)
        empty = root.findViewById(R.id.empty)
        mySearchView = root.findViewById(R.id.searchview)
        mySearchView?.setRightButtonVisibility(true)
        mySearchView?.setLeftIcon(R.drawable.magnify)
        mySearchView?.setOnBackButtonClickListener(object : MySearchView.OnBackButtonClickListener {
            override fun onBackButtonClick() {
                presenter?.doSearch(mySearchView?.text.toString(), true)
            }
        })

        mySearchView?.setOnQueryTextListener(object : MySearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                presenter?.doSearch(query, true)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                presenter?.doSearch(newText, false)
                return false
            }
        })
        val columns = resources.getInteger(R.integer.files_column_count)
        mLayoutManager = GridLayoutManager(requireActivity(), columns, RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = mLayoutManager
        PicassoPauseOnScrollListener.addListener(mRecyclerView)
        tvCurrentDir = root.findViewById(R.id.current_path)
        loading = root.findViewById(R.id.loading)

        animLoad = ObjectAnimator.ofFloat(loading, View.ALPHA, 0.0f).setDuration(1000)
        animLoad?.addListener(object : StubAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                loading?.clearAnimationDrawable()
                loading?.visibility = View.GONE
                loading?.alpha = 1f
            }

            override fun onAnimationCancel(animation: Animator) {
                loading?.clearAnimationDrawable()
                loading?.visibility = View.GONE
                loading?.alpha = 1f
            }
        })

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout?.setOnRefreshListener {
            mSwipeRefreshLayout?.isRefreshing = false
            if (presenter?.canRefresh() == true) {
                presenter?.loadFiles(
                    back = false, caches = false,
                    fromCache = false
                )
            }
        }
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        mAdapter = FileManagerAdapter(requireActivity(), emptyList())
        mAdapter?.setClickListener(this)
        mRecyclerView?.adapter = mAdapter

        addButton = root.findViewById(R.id.add_button)
        addButton?.setOnClickListener {
            presenter?.fireAddClick()
        }
        return root
    }

    override fun onBusy(path: String) {
        PlaceFactory.getFileManagerPlace(
            path,
            true,
            arguments?.getBoolean(Extra.SELECT) == true
        )
            .tryOpenWith(requireActivity())
    }

    override fun updateAddButton(isPasteAction: Boolean) {
        addButton?.setImageResource(if (isPasteAction) R.drawable.content_copy else R.drawable.plus)
    }

    override fun onClick(position: Int, item: FileItem) {
        if (item.type == FileType.folder) {
            val sel = File(item.file_path ?: return)
            if (presenter?.canRefresh() == true) {
                mLayoutManager?.onSaveInstanceState()?.let { presenter?.backupDirectoryScroll(it) }
                presenter?.setCurrent(sel)
            } else {
                PlaceFactory.getFileManagerPlace(
                    sel.absolutePath,
                    true,
                    arguments?.getBoolean(Extra.SELECT) == true
                )
                    .tryOpenWith(requireActivity())
            }
            return
        } else {
            if (arguments?.getBoolean(Extra.SELECT) == true) {
                requireActivity().setResult(
                    RESULT_OK,
                    Intent().setData(Uri.fromFile(item.file_path?.let {
                        File(
                            it
                        )
                    }))
                )
                requireActivity().finish()
            }
        }
    }

    override fun onFixDir(item: FileItem) {
        item.file_path?.let { presenter?.fireFixDirTime(it) }
    }

    override fun onUpdateTimeFile(item: FileItem) {
        val tmp = File(item.file_path ?: return)
        if (tmp.setLastModified(Calendar.getInstance().time.time)) {
            showMessage(R.string.success)
            presenter?.loadFiles(
                back = false, caches = false,
                fromCache = false
            )
        }
    }

    private val requestEnterPin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getParcelableExtraCompat<FileItem>(Extra.PATH)
                ?.let { presenter?.fireDelete(it) }
        }
    }

    private fun startEnterPinActivity(item: FileItem) {
        requestEnterPin.launch(
            EnterPinActivity.getIntent(requireActivity()).putExtra(Extra.PATH, item)
        )
    }

    override fun onDelete(item: FileItem) {
        MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.attention)
            .setMessage(requireActivity().getString(R.string.do_remove, item.file_name))
            .setPositiveButton(R.string.button_yes) { _, _ ->
                if (Settings.get().security().isUsePinForEntrance && Settings.get().security()
                        .hasPinHash
                ) {
                    startEnterPinActivity(item)
                } else {
                    presenter?.fireDelete(item)
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    override fun onCopy(item: FileItem) {
        presenter?.fireSetCopied(item)
    }

    override fun onRename(item: FileItem) {
        displayCreateDialog(item)
    }

    override fun restoreScroll(scroll: Parcelable) {
        mLayoutManager?.onRestoreInstanceState(scroll)
    }

    override fun onBackPressed(): Boolean {
        if (presenter?.canLoadUp() == true) {
            mLayoutManager?.onSaveInstanceState()?.let { presenter?.backupDirectoryScroll(it) }
            presenter?.loadUp()
            mySearchView?.clear()
            return false
        }
        return true
    }

    companion object {
        const val ENTRY_NAME_RESULT = "entry_name_result"
        fun buildArgs(path: String, base: Boolean, isSelect: Boolean): Bundle {
            val args = Bundle()
            args.putString(Extra.PATH, path)
            args.putBoolean(Extra.POSITION, base)
            args.putBoolean(Extra.SELECT, isSelect)
            return args
        }

        fun newInstance(args: Bundle): FileManagerFragment {
            val fragment = FileManagerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun displayData(items: ArrayList<FileItem>) {
        mAdapter?.setItems(items)
    }

    override fun resolveEmptyText(visible: Boolean) {
        empty?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun resolveLoading(visible: Boolean) {
        animationDispose.dispose()
        if (mAnimationLoaded && !visible) {
            mAnimationLoaded = false
            animLoad?.start()
        } else if (!mAnimationLoaded && visible) {
            animLoad?.end()
            animationDispose = Completable.create {
                it.onComplete()
            }.delay(300, TimeUnit.MILLISECONDS).fromIOToMain().subscribe({
                mAnimationLoaded = true
                loading?.visibility = View.VISIBLE
                loading?.alpha = 1f
                loading?.fromRes(
                    R.raw.s_loading,
                    Utils.dp(180f),
                    Utils.dp(180f),
                    intArrayOf(
                        0x333333,
                        CurrentTheme.getColorPrimary(requireActivity()),
                        0x777777,
                        CurrentTheme.getColorSecondary(requireActivity())
                    )
                )
                loading?.playAnimation()
            }, RxUtils.ignore())
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setAnchorView(mRecyclerView)?.setDuration(Toast.LENGTH_LONG)?.showToast(res)
    }

    override fun notifyAllChanged() {
        addButton?.show()
        mAdapter?.notifyDataSetChanged()
    }

    override fun updatePathString(file: String) {
        tvCurrentDir?.text = file
        if (requireActivity() is UpdatableNavigation) {
            (requireActivity() as UpdatableNavigation).onUpdateNavigation()
        }
    }

    override fun onScrollTo(pos: Int) {
        mLayoutManager?.scrollToPosition(pos)
    }

    override fun notifyItemChanged(pos: Int) {
        mAdapter?.notifyItemChanged(pos)
    }

    override fun canBackPressed(): Boolean {
        return presenter?.canLoadUp() == true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            ENTRY_NAME_RESULT, this
        ) { _, result ->
            result.getParcelableCompat<FileItem>(Extra.DATA)?.let { presenter?.fireStore(it) }
        }
    }

    override fun displayCreateDialog(obj: FileItem) {
        val dialog = EntryFileNameDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, obj)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "EntryFileNameDialog")
    }

    class EntryFileNameDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_file_name, null)
            val obj = requireArguments().getParcelableCompat<FileItem>(Extra.DATA)!!

            val mTitle = view.findViewById<TextInputEditText>(R.id.edit_title)

            if (savedInstanceState == null) {
                mTitle.setText(obj.file_name)
            }
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    obj.setTmpNewName(mTitle.text.toString())
                    val res = Bundle()
                    res.putParcelable(Extra.DATA, obj)
                    parentFragmentManager.setFragmentResult(
                        ENTRY_NAME_RESULT,
                        res
                    )
                    dismiss()
                }.create()
        }
    }
}
