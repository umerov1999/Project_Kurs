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
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.getParcelableCompat
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.umerov.project.modalbottomsheetdialogfragment.OptionRequest
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.main.labs.FinanceWallet
import dev.umerov.project.place.PlaceFactory
import dev.umerov.project.util.MessagesReplyItemCallback
import dev.umerov.project.util.ViewUtils

class FinanceWalletsFragment : BaseMvpFragment<FinanceWalletsPresenter, IFinanceWalletsView>(),
    IFinanceWalletsView, FinanceWalletsAdapter.ClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: FinanceWalletsAdapter? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
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
            if (requireArguments().getBoolean(Extra.IS_CREDIT_CARD)) ENTRY_CREDIT_CARD_RESULT else ENTRY_WALLET_RESULT,
            this
        ) { _: String?, result: Bundle ->
            result.getParcelableCompat<FinanceWallet>(Extra.DATA)
                ?.let { presenter?.fireStore(it) }
        }
    }

    override fun displayCreateDialog(obj: FinanceWallet) {
        val dialog = EntryItemDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, obj)
        dialog.arguments = bundle
        dialog.show(
            parentFragmentManager,
            if (requireArguments().getBoolean(Extra.IS_CREDIT_CARD)) "EntryFinanceCreditCardDialog" else "EntryFinanceWalletDialog"
        )
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        FinanceWalletsPresenter(requireArguments().getBoolean(Extra.IS_CREDIT_CARD))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_finance_wallets, container, false)

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout?.setOnRefreshListener {
            mSwipeRefreshLayout?.isRefreshing = false
            presenter?.loadDb()
        }
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)

        mRecyclerView = root.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = layoutManager
        mAdapter = FinanceWalletsAdapter(emptyList())
        mAdapter?.setClickListener(this)
        mRecyclerView?.adapter = mAdapter
        root.findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            presenter?.fireAdd()
        }
        ItemTouchHelper(MessagesReplyItemCallback { o: Int ->
            showMenu(o)
        }).attachToRecyclerView(mRecyclerView)
        return root
    }

    companion object {
        const val ENTRY_WALLET_RESULT = "entry_finance_wallet_result"
        const val ENTRY_CREDIT_CARD_RESULT = "entry_finance_credit_card_result"
        fun newInstance(isCreditCard: Boolean): FinanceWalletsFragment {
            val arg = Bundle()
            arg.putBoolean(Extra.IS_CREDIT_CARD, isCreditCard)
            val fragment = FinanceWalletsFragment()
            fragment.arguments = arg
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

    override fun showOperationFragment(ownerId: Long) {
        PlaceFactory.getFinanceOperationsPlace(ownerId).tryOpenWith(requireActivity())
    }

    override fun displayData(data: List<FinanceWallet>) {
        mAdapter?.setData(data)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    class EntryItemDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_finance_wallet, null)
            val obj = requireArguments().getParcelableCompat<FinanceWallet>(Extra.DATA)!!

            val mTitle = view.findViewById<TextInputEditText>(R.id.edit_title)

            if (savedInstanceState == null) {
                mTitle.setText(obj.title)
            }
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                    obj.setTitle(mTitle.text.toString())
                    val res = Bundle()
                    res.putParcelable(Extra.DATA, obj)
                    parentFragmentManager.setFragmentResult(
                        if (obj.isCreditCard) ENTRY_CREDIT_CARD_RESULT else ENTRY_WALLET_RESULT,
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
            if (requireArguments().getBoolean(Extra.IS_CREDIT_CARD)) "finance_credit_card_options" else "finance_wallet_options"
        ) { _, option ->
            when (option.id) {
                0 -> presenter?.fireEdit(pos)
                1 -> presenter?.fireDelete(pos)
            }
        }
    }

    override fun onClick(position: Int, value: FinanceWallet) {
        presenter?.fireClick(position)
    }

    override fun onLongClick(position: Int, value: FinanceWallet): Boolean {
        presenter?.fireLongClick(position)
        return true
    }

    override fun onDelete(position: Int, value: FinanceWallet) {
        presenter?.fireDeleteAnim(position)
    }

    override fun onEdit(position: Int, title: String?) {
        presenter?.fireEdit(position, title)
    }
}
