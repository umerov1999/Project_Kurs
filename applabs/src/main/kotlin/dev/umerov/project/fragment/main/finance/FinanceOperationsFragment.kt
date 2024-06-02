package dev.umerov.project.fragment.main.finance

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.getParcelableCompat
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.umerov.project.modalbottomsheetdialogfragment.OptionRequest
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.main.labs.FinanceOperation
import dev.umerov.project.util.Utils
import dev.umerov.project.util.ViewUtils
import dev.umerov.project.util.toast.CustomToast


class FinanceOperationsFragment :
    BaseMvpFragment<FinanceOperationsPresenter, IFinanceOperationsView>(),
    IFinanceOperationsView, FinanceOperationsAdapter.ClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: FinanceOperationsAdapter? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = ActivityUtils.supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.finance_operations)
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
            ENTRY_OPERATION_RESULT, this
        ) { _: String?, result: Bundle ->
            result.getParcelableCompat<FinanceOperation>(Extra.DATA)
                ?.let { presenter?.fireStore(it) }
        }
    }

    override fun displayCreateDialog(obj: FinanceOperation) {
        val dialog = EntryItemDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, obj)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "EntryFinanceOperationDialog")
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        FinanceOperationsPresenter(requireArguments().getLong(Extra.OWNER_ID))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_finance_operations, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout?.setOnRefreshListener {
            mSwipeRefreshLayout?.isRefreshing = false
            presenter?.loadDb()
        }
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)

        mRecyclerView = root.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = layoutManager
        mAdapter = FinanceOperationsAdapter(emptyList())
        mAdapter?.setClickListener(this)
        mRecyclerView?.adapter = mAdapter
        root.findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            presenter?.fireAdd()
        }
        return root
    }

    companion object {
        const val ENTRY_OPERATION_RESULT = "entry_finance_operation_result"

        fun buildArgs(ownerId: Long): Bundle {
            val args = Bundle()
            args.putLong(Extra.OWNER_ID, ownerId)
            return args
        }

        fun newInstance(args: Bundle): FinanceOperationsFragment {
            val fragment = FinanceOperationsFragment()
            fragment.arguments = args
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

    override fun displayData(data: List<FinanceOperation>) {
        mAdapter?.setData(data)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    class EntryItemDialog : DialogFragment() {
        private fun fixNumeric(str: String): String {
            return str.replace(",", ".")
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_finance_operation, null)
            val obj = requireArguments().getParcelableCompat<FinanceOperation>(Extra.DATA)!!

            val mTitle = view.findViewById<TextInputEditText>(R.id.edit_title)
            val mDescription = view.findViewById<TextInputEditText>(R.id.edit_description)
            val mCoin = view.findViewById<TextInputEditText>(R.id.edit_coin)
            val mIncome = view.findViewById<MaterialSwitch>(R.id.edit_income)

            if (savedInstanceState == null) {
                mTitle.setText(obj.title)
                mDescription.setText(obj.description)
                mCoin.setText(fixNumeric(String.format(Utils.appLocale, "%.2f", obj.coins)))
                mIncome.isChecked = obj.isIncome

            }
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                    try {
                        obj.setTitle(mTitle.text.toString())
                        obj.setDescription(mDescription.text.toString())
                        obj.setIsIncome(mIncome.isChecked)
                        obj.setCoins(fixNumeric(mCoin.text.toString()).toDouble())
                    } catch (e: Exception) {
                        CustomToast.createCustomToast(requireActivity(), null)
                            ?.showToastError(e.localizedMessage)
                    }
                    val res = Bundle()
                    res.putParcelable(Extra.DATA, obj)
                    parentFragmentManager.setFragmentResult(
                        ENTRY_OPERATION_RESULT,
                        res
                    )
                    dismiss()
                }.create()
        }
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
            "finance_operation_options"
        ) { _, option ->
            when (option.id) {
                0 -> presenter?.fireEdit(pos)
                1 -> presenter?.fireDelete(pos)
            }
        }
    }

    override fun onClick(position: Int, value: FinanceOperation) {
        presenter?.fireClick(position)
    }

    override fun onLongClick(position: Int, value: FinanceOperation): Boolean {
        showMenu(position)
        return true
    }

    override fun onDelete(position: Int, value: FinanceOperation) {
        presenter?.fireDeleteAnim(position)
    }

    override fun onEdit(
        position: Int,
        title: String?,
        description: String?,
        coins: Double,
        isIncoming: Boolean
    ) {
        presenter?.fireEdit(position, title, description, coins, isIncoming)
    }
}
