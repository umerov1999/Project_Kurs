package dev.umerov.project.fragment.main.lab9

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
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
import dev.umerov.project.model.main.labs.Lab9Contact
import dev.umerov.project.util.MessagesReplyItemCallback

class Lab9Fragment : BaseMvpFragment<Lab9Presenter, ILab9View>(),
    ILab9View, Lab9Adapter.ClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: Lab9Adapter? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_9)
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
            ENTRY_CONTACT_RESULT, this
        ) { _, result ->
            result.getParcelableCompat<Lab9Contact>(Extra.DATA)?.let { presenter?.fireStore(it) }
        }
    }

    override fun displayCreateDialog(obj: Lab9Contact) {
        val dialog = EntryContactDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, obj)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "EntryContactDialog")
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab9Presenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab9, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        mRecyclerView = root.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = layoutManager
        mAdapter = Lab9Adapter(emptyList())
        mAdapter?.setClickListener(this)
        mRecyclerView?.adapter = mAdapter

        root.findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            presenter?.fireAdd()
        }

        ItemTouchHelper(MessagesReplyItemCallback { o ->
            showMenu(o)
        }).attachToRecyclerView(mRecyclerView)
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
            "lab9_options"
        ) { _, option ->
            when (option.id) {
                0 -> presenter?.fireEdit(pos)
                1 -> presenter?.fireDelete(pos)
            }
        }
    }

    companion object {
        const val ENTRY_CONTACT_RESULT = "entry_contact_result"
        fun newInstance(): Lab9Fragment {
            return Lab9Fragment()
        }
    }

    override fun notifyItemChanged(index: Int) {
        mAdapter?.notifyItemChanged(index)
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        mAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun notifyDataRemoved(position: Int, count: Int) {
        mAdapter?.notifyItemRangeRemoved(position, count)
    }

    override fun displayData(data: List<Lab9Contact>) {
        mAdapter?.setData(data)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    class EntryContactDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_lab9_contact, null)
            val obj = requireArguments().getParcelableCompat<Lab9Contact>(Extra.DATA)!!

            val mPhone = view.findViewById<TextInputEditText>(R.id.edit_phone)
            val mMail = view.findViewById<TextInputEditText>(R.id.edit_mail)

            if (savedInstanceState == null) {
                mPhone.setText(obj.contact)
                mMail.setText(obj.email)
            }
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    obj.contact = mPhone.text.toString()
                    obj.email = mMail.text.toString()
                    val res = Bundle()
                    res.putParcelable(Extra.DATA, obj)
                    parentFragmentManager.setFragmentResult(
                        ENTRY_CONTACT_RESULT,
                        res
                    )
                    dismiss()
                }.create()
        }
    }

    override fun onClick(position: Int, value: Lab9Contact) {
        presenter?.fireClick(position)
    }

    override fun onDelete(position: Int, value: Lab9Contact) {
        presenter?.fireDeleteAnim(position)
    }

    override fun onEdit(position: Int, contact: String?, mail: String?) {
        presenter?.fireEdit(position, contact, mail)
    }
}
