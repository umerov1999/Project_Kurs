package dev.umerov.project.fragment.main.coin

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import dev.umerov.project.activity.ActivityUtils
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.getParcelableCompat
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.umerov.project.modalbottomsheetdialogfragment.OptionRequest
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.db.CoinOperation
import dev.umerov.project.model.db.CoinOperationType
import dev.umerov.project.model.db.Register
import dev.umerov.project.util.MessagesReplyItemCallback
import dev.umerov.project.util.Utils
import dev.umerov.project.util.ViewUtils
import dev.umerov.project.util.toast.CustomToast

class CoinFragment :
    BaseMvpFragment<CoinPresenter, ICoinView>(),
    CoinOperationAdapter.ClickListener,
    ICoinView {
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mBalance: TextView? = null
    private var mCoinAdapter: CoinOperationAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_coin, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        mBalance = root.findViewById(R.id.balance)

        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }

        root.findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            presenter?.fireAdd()
        }

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        mCoinAdapter = CoinOperationAdapter(requireActivity(), emptyList())
        mCoinAdapter?.setClickListener(this)
        recyclerView.adapter = mCoinAdapter

        ItemTouchHelper(MessagesReplyItemCallback { o ->
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
                "coin_options"
            ) { _, option ->
                when (option.id) {
                    0 -> presenter?.fireEdit(o)
                    1 -> presenter?.fireDelete(o)
                }
            }
        }).attachToRecyclerView(recyclerView)
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            Extra.DATA_RESULT, this
        ) { _, result ->
            result.getParcelableCompat<CoinOperation>(Extra.DATA)?.let { presenter?.fireStore(it) }
        }
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        CoinPresenter(requireArguments().getInt(Extra.TYPE, 1))

    override fun onResume() {
        super.onResume()

        val isTake = requireArguments().getInt(Extra.TYPE, 1) == 1
        if (requireActivity() is OnSectionResumeCallback) {
            if (isTake) {
                (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.TAKE)
            } else {
                (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.PASTE)
            }
        }
        val actionBar = ActivityUtils.supportToolbarFor(this)
        if (actionBar != null) {
            if (isTake) {
                actionBar.setTitle(R.string.take_money)
            } else {
                actionBar.setTitle(R.string.paste_money)
            }
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun displayList(operations: List<CoinOperation>) {
        mCoinAdapter?.setItems(operations)
    }

    override fun displayRegister(register: Register?) {
        mBalance?.text =
            requireActivity().getString(
                R.string.rub,
                String.format(Utils.appLocale, "%.2f", register?.coinBalance)
            )
    }

    override fun notifyListChanged() {
        mCoinAdapter?.notifyDataSetChanged()
    }

    override fun displayLoading(loading: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = loading
    }

    override fun displayCreateDialog(operation: CoinOperation) {
        val dialog = CreateOrDeleteOperationDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, operation)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "CreateOrDeleteOperationDialog")
    }

    override fun notifyItemChanged(index: Int) {
        mCoinAdapter?.notifyItemChanged(index)
    }

    override fun notifyDataAdded(position: Int, count: Int) {
        mCoinAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun notifyDataRemoved(position: Int, count: Int) {
        mCoinAdapter?.notifyItemRangeRemoved(position, count)
    }

    companion object {
        fun newInstance(@CoinOperationType operationType: Int): CoinFragment {
            val fragment = CoinFragment()
            val bundle = Bundle()
            bundle.putInt(Extra.TYPE, operationType)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onClick(position: Int, operation: CoinOperation) {
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
            "coin_options"
        ) { _, option ->
            when (option.id) {
                0 -> presenter?.fireEdit(position)
                1 -> presenter?.fireDelete(position)
            }
        }
    }

    class CreateOrDeleteOperationDialog : DialogFragment() {
        private fun fixNumeric(str: String): String {
            return str.replace(",", ".")
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_operation, null)
            val obj = requireArguments().getParcelableCompat<CoinOperation>(Extra.DATA)!!

            val mTitle = view.findViewById<TextInputEditText>(R.id.edit_title)
            val mComment = view.findViewById<TextInputEditText>(R.id.edit_comment)
            val mCoin = view.findViewById<TextInputEditText>(R.id.edit_coin)

            if (savedInstanceState == null) {
                mTitle.setText(obj.title)
                mComment.setText(obj.comment)
                mCoin.setText(fixNumeric(String.format(Utils.appLocale, "%.2f", obj.coin)))
            }
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    try {
                        obj.title = mTitle.text.toString()
                        obj.comment = mComment.text.toString()
                        obj.coin = fixNumeric(mCoin.text.toString()).toDouble()
                        if (obj.coin <= 0) {
                            CustomToast.createCustomToast(requireActivity(), null)
                                ?.showToastError(R.string.coin_must_over_zero)
                            return@setPositiveButton
                        }
                    } catch (e: Exception) {
                        CustomToast.createCustomToast(requireActivity(), null)
                            ?.showToastError(e.localizedMessage)
                    }
                    val res = Bundle()
                    res.putParcelable(Extra.DATA, obj)
                    parentFragmentManager.setFragmentResult(
                        Extra.DATA_RESULT,
                        res
                    )
                    dismiss()
                }.create()
        }
    }
}
