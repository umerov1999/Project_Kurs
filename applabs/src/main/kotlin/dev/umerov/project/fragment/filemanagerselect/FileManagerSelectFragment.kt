package dev.umerov.project.fragment.filemanagerselect

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.StubAnimatorListener
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.fromIOToMain
import dev.umerov.project.listener.BackPressCallback
import dev.umerov.project.listener.PicassoPauseOnScrollListener
import dev.umerov.project.listener.UpdatableNavigation
import dev.umerov.project.model.FileItemSelect
import dev.umerov.project.settings.CurrentTheme
import dev.umerov.project.util.Utils
import dev.umerov.project.util.rxutils.RxUtils
import dev.umerov.project.util.toast.CustomToast
import dev.umerov.project.view.MySearchView
import dev.umerov.project.view.natives.rlottie.RLottieImageView
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.util.concurrent.TimeUnit

class FileManagerSelectFragment :
    BaseMvpFragment<FileManagerSelectPresenter, IFileManagerSelectView>(),
    IFileManagerSelectView, FileManagerSelectAdapter.ClickListener, BackPressCallback {
    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: GridLayoutManager? = null
    private var empty: TextView? = null
    private var loading: RLottieImageView? = null
    private var tvCurrentDir: TextView? = null
    private var mAdapter: FileManagerSelectAdapter? = null
    private var mSelected: FloatingActionButton? = null
    private var mHeader: MaterialTextView? = null

    private var animationDispose = Disposable.disposed()
    private var mAnimationLoaded = false
    private var animLoad: ObjectAnimator? = null
    private var mySearchView: MySearchView? = null

    override fun onBackPressed(): Boolean {
        if (presenter?.canLoadUp() == true) {
            mLayoutManager?.onSaveInstanceState()?.let { presenter?.backupDirectoryScroll(it) }
            presenter?.loadUp()
            mySearchView?.clear()
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        animationDispose.dispose()
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = FileManagerSelectPresenter(
        File(requireArguments().getString(Extra.PATH)!!),
        requireArguments().getString(Extra.EXT)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_file_select_explorer, container, false)
        mRecyclerView = root.findViewById(R.id.list)
        empty = root.findViewById(R.id.empty)

        mySearchView = root.findViewById(R.id.searchview)
        mySearchView?.setRightButtonVisibility(false)
        mySearchView?.setLeftIcon(R.drawable.magnify)
        mySearchView?.setOnBackButtonClickListener(object : MySearchView.OnBackButtonClickListener {
            override fun onBackButtonClick() {
                presenter?.doSearch(mySearchView?.text.toString())
            }
        })

        mySearchView?.setOnQueryTextListener(object : MySearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                presenter?.doSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                presenter?.doSearch(newText)
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

        mHeader = root.findViewById(R.id.select_header)
        mHeader?.visibility =
            if (arguments?.getBoolean(Extra.HIDE_TITLE, false) == true) View.GONE else View.VISIBLE

        mAdapter = FileManagerSelectAdapter(emptyList())
        mAdapter?.setClickListener(this)
        mRecyclerView?.adapter = mAdapter

        mSelected = root.findViewById(R.id.selected_button)
        mSelected?.setOnClickListener {
            val retIntent = Intent()
            retIntent.putExtra(Extra.PATH, presenter?.getCurrentDir())
            requireActivity().setResult(Activity.RESULT_OK, retIntent)
            requireActivity().finish()
        }
        return root
    }

    override fun onClick(position: Int, item: FileItemSelect) {
        if (item.isDir) {
            val sel = File(item.file_path ?: return)
            mLayoutManager?.onSaveInstanceState()?.let { presenter?.backupDirectoryScroll(it) }
            presenter?.setCurrent(sel)
        } else {
            val retIntent = Intent()
            retIntent.putExtra(Extra.PATH, item.file_path)
            requireActivity().setResult(Activity.RESULT_OK, retIntent)
            requireActivity().finish()
        }
    }

    override fun displayData(items: ArrayList<FileItemSelect>) {
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
        CustomToast.createCustomToast(requireActivity(), mRecyclerView)
            ?.setDuration(Toast.LENGTH_LONG)?.showToast(res)
    }

    override fun updateSelectVisibility(visible: Boolean) {
        mSelected?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun updateHeader(ext: String?) {
        if (arguments?.containsKey(Extra.TITLE) == true) {
            mHeader?.text = requireArguments().getString(Extra.TITLE)
            return
        }
        if ("dirs" == ext) {
            mHeader?.setText(R.string.select_folder)
        } else {
            mHeader?.text = getString(R.string.select_file, ext ?: "*")
        }
    }

    override fun notifyAllChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun updatePathString(file: String) {
        tvCurrentDir?.text = file
        if (requireActivity() is UpdatableNavigation) {
            (requireActivity() as UpdatableNavigation).onUpdateNavigation()
        }
    }

    override fun restoreScroll(scroll: Parcelable) {
        mLayoutManager?.onRestoreInstanceState(scroll)
    }

    override fun onScrollTo(pos: Int) {
        mLayoutManager?.scrollToPosition(pos)
    }

    override fun notifyItemChanged(pos: Int) {
        mAdapter?.notifyItemChanged(pos)
    }
}
