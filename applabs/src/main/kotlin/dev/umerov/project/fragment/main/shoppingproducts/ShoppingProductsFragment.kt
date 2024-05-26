package dev.umerov.project.fragment.main.shoppingproducts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
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
import dev.umerov.project.model.main.labs.Product
import dev.umerov.project.model.main.labs.ProductUnit
import dev.umerov.project.model.main.labs.ShoppingList
import dev.umerov.project.util.AppPerms
import dev.umerov.project.util.AppPerms.requestPermissionsResultAbs
import dev.umerov.project.util.AppTextUtils
import dev.umerov.project.util.Utils
import dev.umerov.project.util.ViewUtils
import dev.umerov.project.util.toast.CustomToast.Companion.createCustomToast
import dev.umerov.project.view.RoundCornerLinearView
import dev.umerov.project.view.ShoppingListTextInputEditText
import java.util.Calendar
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.Calendar.SECOND

class ShoppingProductsFragment :
    BaseMvpFragment<ShoppingProductsPresenter, IShoppingProductsView>(),
    IShoppingProductsView, ShoppingProductsAdapter.ClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ShoppingProductsAdapter? = null
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
            ENTRY_SHOPPING_PRODUCT_RESULT, this
        ) { _: String?, result: Bundle ->
            result.getParcelableCompat<Product>(Extra.DATA)
                ?.let { presenter?.fireStore(it, false) }
        }
    }

    override fun displayCreateDialog(obj: Product) {
        val dialog = EntryShoppingProductsDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, obj)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "EntryShoppingProductsDialog")
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        ShoppingProductsPresenter(
            requireActivity(),
            requireArguments().getParcelableCompat<ShoppingList>(Extra.PATH)!!
        )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestNPermission = requestPermissionsResultAbs(
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS
        ), {
            createCustomToast(requireActivity(), null)?.showToast(R.string.success)
        }, {
            createCustomToast(requireActivity(), null)?.showToast(R.string.cancel)
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_shopping_product, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        mRecyclerView = root.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = layoutManager
        mAdapter = ShoppingProductsAdapter(requireActivity(), emptyList())
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

        val list = requireArguments().getParcelableCompat<ShoppingList>(Extra.PATH)
        list?.let {
            root.findViewById<RoundCornerLinearView>(R.id.item_view_list).setViewColor(it.color)
            root.findViewById<MaterialTextView>(R.id.item_created).text =
                AppTextUtils.getDateFromUnixTimeShorted(requireActivity(), it.creationDate)
            root.findViewById<MaterialTextView>(R.id.item_title).text =
                requireActivity().getString(R.string.title_param, it.title)
            root.findViewById<MaterialTextView>(R.id.item_description).text =
                requireActivity().getString(R.string.description_param, it.description)
        }

        root.findViewById<MaterialButton>(R.id.schedule_notification).setOnClickListener {
            if (!AppPerms.hasNotificationPermissionSimple(requireActivity())) {
                if (Utils.hasTiramisu()) {
                    requestNPermission.launch()
                }
            } else {
                val datePicker = MaterialDatePicker.Builder.datePicker().build()
                datePicker.addOnPositiveButtonClickListener { dt ->
                    val tmpDt = Calendar.getInstance()
                    tmpDt.timeInMillis = dt
                    tmpDt.set(SECOND, 0)
                    tmpDt.set(MINUTE, 0)
                    tmpDt.set(HOUR_OF_DAY, 0)
                    val materialTimePicker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .build()

                    materialTimePicker.addOnPositiveButtonClickListener {
                        val fa =
                            tmpDt.timeInMillis + materialTimePicker.hour * 3600000 + materialTimePicker.minute * 60000
                        if (fa > Calendar.getInstance().timeInMillis) {
                            presenter?.fireScheduleNotification(fa)
                        }
                    }
                    materialTimePicker.show(requireActivity().supportFragmentManager, "time_pick")
                }
                datePicker.show(requireActivity().supportFragmentManager, "date_pick")
            }
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
            "shopping_products_options"
        ) { _, option ->
            when (option.id) {
                0 -> presenter?.fireEdit(pos)
                1 -> presenter?.fireDelete(pos)
            }
        }
    }

    companion object {
        const val ENTRY_SHOPPING_PRODUCT_RESULT = "entry_shopping_product_result"
        fun buildArgs(shoppingList: ShoppingList): Bundle {
            val args = Bundle()
            args.putParcelable(Extra.PATH, shoppingList)
            return args
        }

        fun newInstance(args: Bundle): ShoppingProductsFragment {
            val fragment = ShoppingProductsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun notifyItemChanged(index: Int) {
        mAdapter?.notifyItemChanged(index)
    }

    override fun notifyItemMoved(fromPosition: Int, toPosition: Int) {
        mAdapter?.notifyItemMoved(fromPosition, toPosition)
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

    override fun displayData(data: List<Product>) {
        mAdapter?.setData(data)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    class EntryShoppingProductsDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_shopping_product, null)
            val obj = requireArguments().getParcelableCompat<Product>(Extra.DATA)!!

            val mName = view.findViewById<ShoppingListTextInputEditText>(R.id.edit_name)
            val mCount = view.findViewById<TextInputEditText>(R.id.edit_count)
            val mUnit = view.findViewById<AppCompatSpinner>(R.id.edit_unit)

            val unitAdapter =
                ArrayAdapter<String>(
                    requireActivity(),
                    R.layout.item_array_text,
                    R.id.itemTextView
                )
            unitAdapter.add(requireActivity().getString(R.string.pieces))
            unitAdapter.add(requireActivity().getString(R.string.liters))
            unitAdapter.add(requireActivity().getString(R.string.kilograms))
            unitAdapter.add(requireActivity().getString(R.string.grams))
            unitAdapter.add(requireActivity().getString(R.string.meters))
            mUnit.adapter = unitAdapter

            if (savedInstanceState == null) {
                mName.setText(obj.name)
                mCount.setText(obj.count.toString())
                mUnit.setSelection(obj.unit)
            }
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                    obj.setName(mName.text.toString())
                    try {
                        obj.setCount(mCount.text.toString().toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    obj.setUnit(mUnit.selectedItemPosition)
                    val res = Bundle()
                    res.putParcelable(Extra.DATA, obj)
                    parentFragmentManager.setFragmentResult(
                        ENTRY_SHOPPING_PRODUCT_RESULT,
                        res
                    )
                    dismiss()
                }.create()
        }
    }

    override fun onClick(position: Int, value: Product) {
        presenter?.fireClick(position)
    }

    override fun onLongClick(position: Int, value: Product): Boolean {
        showMenu(position)
        return true
    }

    override fun onDelete(position: Int, value: Product) {
        presenter?.fireDeleteAnim(position)
    }

    override fun onEdit(position: Int, name: String?, count: Int, @ProductUnit unit: Int) {
        presenter?.fireEdit(position, name, count, unit)
    }

    override fun onBought(position: Int, isBought: Boolean) {
        presenter?.fireBought(position, isBought)
    }
}
