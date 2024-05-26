package dev.umerov.project.fragment.main.lab8

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import dev.umerov.project.R
import dev.umerov.project.activity.ActivityFeatures
import dev.umerov.project.activity.ActivityUtils.supportToolbarFor
import dev.umerov.project.fragment.base.BaseMvpFragment
import dev.umerov.project.listener.OnSectionResumeCallback
import dev.umerov.project.model.SectionItem
import dev.umerov.project.model.main.labs.Lab8Currency


class Lab8Fragment : BaseMvpFragment<Lab8Presenter, ILab8View>(),
    ILab8View {
    private var inputSum: TextInputEditText? = null
    private var result: TextInputEditText? = null
    private var currency: AppCompatSpinner? = null
    private var currencyAdapter: ArrayAdapter<String>? = null
    private var convertTo: AppCompatSpinner? = null
    private var convertToAdapter: ArrayAdapter<String>? = null
    private var purchase: MaterialRadioButton? = null
    private var sale: MaterialRadioButton? = null
    private var exchangeRate: TextInputEditText? = null
    private var calculate: MaterialButton? = null
    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(SectionItem.MAIN)
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.lab_8)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = Lab8Presenter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_lab8, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        currencyAdapter =
            ArrayAdapter(requireActivity(), R.layout.item_array_text, R.id.itemTextView)
        convertToAdapter =
            ArrayAdapter(requireActivity(), R.layout.item_array_text, R.id.itemTextView)

        inputSum = root.findViewById(R.id.edit_input_sum)
        result = root.findViewById(R.id.edit_result)
        currency = root.findViewById(R.id.item_currency)
        convertTo = root.findViewById(R.id.item_convert_to)
        purchase = root.findViewById(R.id.item_purchase)
        sale = root.findViewById(R.id.item_sale)
        exchangeRate = root.findViewById(R.id.edit_exchange_rate)
        calculate = root.findViewById(R.id.item_calculate)

        currency?.adapter = currencyAdapter
        convertTo?.adapter = convertToAdapter

        currency?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                presenter?.fireUpdateCurrencySelection(position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        convertTo?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                presenter?.fireUpdateConvertTo(position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        purchase?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                presenter?.fireUpdatePurchase(true)
            }
        }

        sale?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                presenter?.fireUpdatePurchase(false)
            }
        }

        exchangeRate?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                presenter?.updateExchangeRateText(s.toString())
            }
        })

        inputSum?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                presenter?.fireUpdateSum(s.toString())
            }
        })
        calculate?.setOnClickListener {
            presenter?.fireGenResult()
        }
        return root
    }

    companion object {
        fun newInstance(): Lab8Fragment {
            return Lab8Fragment()
        }
    }

    override fun showMessage(@StringRes res: Int) {
        customToast?.setDuration(Toast.LENGTH_SHORT)?.showToast(res)
    }

    override fun display(
        currencyList: List<Lab8Currency>,
        currencySelection: Int,
        convertToSelection: Int,
        isPurchase: Boolean,
        exchangeRateText: String,
        sum: String?,
        resultText: String?
    ) {
        currencyAdapter?.clear()
        convertToAdapter?.clear()
        for (i in currencyList) {
            currencyAdapter?.add(i.title)
            convertToAdapter?.add(i.title)
        }
        currencyAdapter?.notifyDataSetChanged()
        convertToAdapter?.notifyDataSetChanged()

        currency?.setSelection(currencySelection)
        convertTo?.setSelection(convertToSelection)

        if (isPurchase) {
            purchase?.isChecked = true
        } else {
            sale?.isChecked = true
        }
        exchangeRate?.setText(exchangeRateText)
        inputSum?.setText(sum)
        result?.setText(resultText)
    }

    override fun updateExchangeRateText(exchangeRateText: String) {
        exchangeRate?.setText(exchangeRateText)
    }

    override fun updateSum(sum: String?) {
        inputSum?.setText(sum)
    }

    override fun updateResult(resultText: String?) {
        result?.setText(resultText)
    }
}
