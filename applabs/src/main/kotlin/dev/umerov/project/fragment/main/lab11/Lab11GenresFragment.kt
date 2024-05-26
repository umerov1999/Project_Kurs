package dev.umerov.project.fragment.main.lab11

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.getParcelableCompat
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.umerov.project.modalbottomsheetdialogfragment.OptionRequest
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.main.labs.Lab11Genre

class Lab11GenresFragment : BaseMvpFragment<Lab11GenresPresenter, ILab11GenresView>(),
    ILab11GenresView, Lab11GenresAdapter.ClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: Lab11GenresAdapter? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_11_genres)
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
            ENTRY_GENRE_RESULT, this
        ) { _: String?, result: Bundle ->
            result.getParcelableCompat<Lab11Genre>(Extra.DATA)?.let { presenter?.fireStore(it) }
        }
    }

    override fun displayCreateDialog(obj: Lab11Genre) {
        val dialog = EntryGenreDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, obj)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "EntryGenreDialog")
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab11GenresPresenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab11_genres, container, false)
        if (!requireArguments().getBoolean(EXTRA_HIDE_TOOLBAR, false)) {
            (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        } else {
            root.findViewById<View>(R.id.toolbar).visibility = View.GONE
        }

        mRecyclerView = root.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = layoutManager
        mAdapter = Lab11GenresAdapter(emptyList())
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
            "lab11_genres_options"
        ) { _, option ->
            when (option.id) {
                0 -> presenter?.fireEdit(pos)
                1 -> presenter?.fireDelete(pos)
            }
        }
    }

    companion object {
        const val ENTRY_GENRE_RESULT = "entry_genre_result"
        fun newInstance(hasTab: Boolean): Lab11GenresFragment {
            val fragment = Lab11GenresFragment()
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

    override fun displayData(data: List<Lab11Genre>) {
        mAdapter?.setData(data)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    class EntryGenreDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_lab11_genre, null)
            val obj = requireArguments().getParcelableCompat<Lab11Genre>(Extra.DATA)!!

            val mName = view.findViewById<TextInputEditText>(R.id.edit_name)
            if (savedInstanceState == null) {
                mName.setText(obj.name)
            }
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                    obj.setName(mName.text.toString())
                    val res = Bundle()
                    res.putParcelable(Extra.DATA, obj)
                    parentFragmentManager.setFragmentResult(
                        ENTRY_GENRE_RESULT,
                        res
                    )
                    dismiss()
                }.create()
        }
    }

    override fun onClick(position: Int, value: Lab11Genre) {
        presenter?.fireClick(position)
    }

    override fun onLongClick(position: Int, value: Lab11Genre): Boolean {
        showMenu(position)
        return true
    }

    override fun onDelete(position: Int, value: Lab11Genre) {
        presenter?.fireDeleteAnim(position)
    }

    override fun onEdit(position: Int, name: String?) {
        presenter?.fireEdit(position, name)
    }
}
