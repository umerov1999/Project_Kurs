package dev.umerov.project.fragment.main.staff

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dev.umerov.project.Extra
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.getParcelableCompat
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.umerov.project.modalbottomsheetdialogfragment.OptionRequest
import dev.umerov.project.model.Human
import dev.umerov.project.model.SectionItem
import dev.umerov.project.module.BufferWriteNative
import dev.umerov.project.module.ProjectNative
import dev.umerov.project.module.thorvg.ThorVGRender
import dev.umerov.project.util.MessagesReplyItemCallback

class StaffFragment : BaseMvpFragment<StaffPresenter, IStaffView>(),
    IStaffView, StaffAdapter.ClickListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: StaffAdapter? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.staff)
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
        ) { _: String?, result: Bundle ->
            result.getParcelableCompat<Human>(Extra.DATA)?.let { presenter?.fireStore(it) }
        }
    }

    override fun displayCreateDialog(obj: Human) {
        val dialog = EntryStaffDialog()
        val bundle = Bundle()
        bundle.putParcelable(Extra.DATA, obj)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, "EntryStaffDialog")
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = StaffPresenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_staff, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        mRecyclerView = root.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = layoutManager
        mAdapter = StaffAdapter(requireActivity(), emptyList())
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

    private fun showMenu(pos: Int) {
        presenter?.fireMenuOpen(pos, true)
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
            "staff_options", { _, option ->
                presenter?.fireMenuOpen(pos, false)
                when (option.id) {
                    0 -> presenter?.fireEdit(pos)
                    1 -> {
                        MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.attention)
                            .setMessage(requireActivity().getString(R.string.do_remove, ""))
                            .setPositiveButton(R.string.button_yes) { _: DialogInterface?, _: Int ->
                                presenter?.fireDelete(pos)
                            }
                            .setNegativeButton(R.string.button_cancel, null)
                            .show()
                    }
                }
            }, {
                presenter?.fireMenuOpen(pos, false)
            })
    }

    companion object {
        const val ENTRY_CONTACT_RESULT = "entry_contact_result"
        fun newInstance(): StaffFragment {
            return StaffFragment()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun notifyDataSetChanged() {
        mAdapter?.notifyDataSetChanged()
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

    override fun displayData(data: List<Human>) {
        mAdapter?.setData(data)
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    class GenderAdapter(
        context: Context,
        @LayoutRes resource: Int,
        @IdRes textViewResourceId: Int
    ) : ArrayAdapter<Boolean>(context, resource, textViewResourceId) {
        private var maleIcon: BitmapDrawable? = null
        private var femaleIcon: BitmapDrawable? = null

        init {
            val bufferMale = BufferWriteNative(8192)
            bufferMale.putStream(ProjectNative.appContext.assets.open("male-svgrepo-com.svg_lz4"))
            maleIcon =
                BitmapDrawable(context.resources, ThorVGRender.createBitmap(bufferMale, 100, 100))

            val bufferFemale = BufferWriteNative(8192)
            bufferFemale.putStream(ProjectNative.appContext.assets.open("female-svgrepo-com.svg_lz4"))
            femaleIcon =
                BitmapDrawable(context.resources, ThorVGRender.createBitmap(bufferFemale, 100, 100))
        }

        @SuppressLint("ViewHolder")
        override fun getView(
            position: Int, convertView: View?,
            parent: ViewGroup
        ): View {
            val gender = getItem(position)
            val view = View.inflate(context, R.layout.item_gender, null)
            val mGenderText = view.findViewById<MaterialTextView>(R.id.item_gender_text)
            val mGenderIcon = view.findViewById<ShapeableImageView>(R.id.item_gender)
            mGenderIcon.setImageDrawable(if (gender == true) maleIcon else femaleIcon)
            mGenderText.setText(if (gender == true) R.string.male else R.string.female)
            return view
        }

        override fun getDropDownView(
            position: Int, convertView: View?,
            parent: ViewGroup
        ): View {
            val gender = getItem(position)
            val view = View.inflate(context, R.layout.item_gender, null)
            val mGenderText = view.findViewById<MaterialTextView>(R.id.item_gender_text)
            val mGenderIcon = view.findViewById<ShapeableImageView>(R.id.item_gender)
            mGenderIcon.setImageDrawable(if (gender == true) maleIcon else femaleIcon)
            mGenderText.setText(if (gender == true) R.string.male else R.string.female)
            return view
        }
    }

    class EntryStaffDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = View.inflate(requireActivity(), R.layout.entry_staff, null)
            val obj = requireArguments().getParcelableCompat<Human>(Extra.DATA)!!

            val mFirstname = view.findViewById<TextInputEditText>(R.id.edit_firstname)
            val mLastname = view.findViewById<TextInputEditText>(R.id.edit_lastname)
            val mGender = view.findViewById<AppCompatSpinner>(R.id.edit_gender)
            val mBorn = view.findViewById<DatePicker>(R.id.edit_born)

            val genderAdapter =
                GenderAdapter(requireActivity(), R.layout.item_gender, R.id.item_gender_text)
            genderAdapter.add(true)
            genderAdapter.add(false)
            mGender.adapter = genderAdapter

            if (savedInstanceState == null) {
                mFirstname.setText(obj.firstName)
                mLastname.setText(obj.lastName)
                mGender.setSelection(if (obj.gender) 0 else 1)
                obj.birthDay?.let {
                    mBorn.updateDate(
                        it.year,
                        it.month + 1,
                        it.dayOfMonth
                    )
                }
            }
            return MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                    obj.setFirstName(mFirstname.text.toString())
                    obj.setLastName(mLastname.text.toString())
                    obj.setGender(mGender.selectedItemPosition == 0)
                    obj.setBirthDay(mBorn.dayOfMonth, mBorn.month, mBorn.year)
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

    override fun onClick(position: Int, value: Human) {
        showMenu(position)
    }
}
