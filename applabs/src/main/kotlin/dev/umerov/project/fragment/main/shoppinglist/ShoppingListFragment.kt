package dev.umerov.project.fragment.main.shoppinglist

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import dev.umerov.project.model.main.labs.ShoppingList
import dev.umerov.project.place.PlaceFactory
import dev.umerov.project.util.MessagesReplyItemCallback
import dev.umerov.project.util.ViewUtils
import dev.umerov.project.view.ShoppingListTextInputEditText

class ShoppingListFragment : BaseMvpFragment<ShoppingListPresenter, IShoppingListView>(),
    IShoppingListView, ShoppingListAdapter.ClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ShoppingListAdapter? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.shopping_list)
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
            ENTRY_SHOPPING_LIST_RESULT, this
        ) { _: String?, result: Bundle ->
            result.getParcelableCompat<ShoppingList>(Extra.DATA)
                ?.let { presenter?.fireStore(it) }
        }
    }

    override fun displayCreateDialog(obj: ShoppingList) {
        val dialog = EntryShoppingListDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, obj)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "EntryShoppingListDialog")
    }

    override fun showProductFragment(shoppingList: ShoppingList) {
        PlaceFactory.getShoppingProductPlace(shoppingList).tryOpenWith(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = ShoppingListPresenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_shopping_list, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        mRecyclerView = root.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = layoutManager
        mAdapter = ShoppingListAdapter(emptyList())
        mAdapter?.setClickListener(this)
        mRecyclerView?.adapter = mAdapter
        root.findViewById<FloatingActionButton>(R.id.add).setOnClickListener {
            presenter?.fireAdd()
        }

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout?.setOnRefreshListener {
            mSwipeRefreshLayout?.isRefreshing = false
            presenter?.loadDb()
        }
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        ItemTouchHelper(MessagesReplyItemCallback { o: Int ->
            presenter?.fireExpand(o)
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
            "shopping_list_options"
        ) { _, option ->
            when (option.id) {
                0 -> presenter?.fireEdit(pos)
                1 -> presenter?.fireDelete(pos)
            }
        }
    }

    companion object {
        const val ENTRY_SHOPPING_LIST_RESULT = "entry_shopping_list_result"
        fun newInstance(): ShoppingListFragment {
            return ShoppingListFragment()
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

    override fun displayData(data: List<ShoppingList>) {
        mAdapter?.setData(data)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    class EntryShoppingListDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_shopping_list, null)
            val obj = requireArguments().getParcelableCompat<ShoppingList>(Extra.DATA)!!

            val mTitle = view.findViewById<ShoppingListTextInputEditText>(R.id.edit_title)
            val mDescription =
                view.findViewById<ShoppingListTextInputEditText>(R.id.edit_description)

            if (savedInstanceState == null) {
                mTitle.setText(obj.title)
                mDescription.setText(obj.description)
            }
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                    obj.setTitle(mTitle.text.toString())
                    obj.setDescription(mDescription.text.toString())
                    val res = Bundle()
                    res.putParcelable(Extra.DATA, obj)
                    parentFragmentManager.setFragmentResult(
                        ENTRY_SHOPPING_LIST_RESULT,
                        res
                    )
                    dismiss()
                }.create()
        }
    }

    override fun onClick(position: Int, value: ShoppingList) {
        presenter?.fireClick(position)
    }

    override fun onLongClick(position: Int, value: ShoppingList): Boolean {
        showMenu(position)
        return true
    }

    override fun onDelete(position: Int, value: ShoppingList) {
        presenter?.fireDeleteAnim(position)
    }

    override fun onEdit(position: Int, title: String?, description: String?) {
        presenter?.fireEdit(position, title, description)
    }
}
