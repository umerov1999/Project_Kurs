package dev.umerov.project.fragment.main.lab8

import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.model.main.labs.Lab8Currency

class Lab8Presenter : RxSupportPresenter<ILab8View>() {
    private var isFirst = true
    private var work = false

    private val currency =
        listOf(
            Lab8Currency("€", 101.90, 103.29),
            Lab8Currency("$", 93.35, 93.79),
            Lab8Currency("₽", 1.0, 1.0)
        )

    private var currencySelection: Int = 2
    private var convertToSelection: Int = 0
    private var isPurchase = true
    private var sum: String? = null
    private var result: String? = null
    private var exchangeRateText: String = (currency[2].purchase / currency[0].purchase).toString()

    private fun calcRate() {
        val objCurrency = currency[currencySelection]
        val objConvertTo = currency[convertToSelection]

        val curCurrency = if (isPurchase) objCurrency.purchase else objCurrency.sale
        val curConvertTo = if (isPurchase) objConvertTo.purchase else objConvertTo.sale
        exchangeRateText = (curCurrency / curConvertTo).toString()
        view?.updateExchangeRateText(exchangeRateText)
    }

    fun fireGenResult() {
        try {
            result = ((sum ?: "0").toDouble() * exchangeRateText.toDouble()).toString()
            view?.updateResult(result)
        } catch (e: Throwable) {
            view?.showThrowable(e)
        }
    }

    fun updateExchangeRateText(exchangeRateText: String) {
        if (work) {
            return
        }
        this.exchangeRateText = exchangeRateText
    }

    fun fireUpdateSum(sum: String) {
        if (work) {
            return
        }
        this.sum = sum
    }

    fun fireUpdateCurrencySelection(pos: Int) {
        if (work) {
            return
        }
        currencySelection = pos
        calcRate()
    }

    fun fireUpdateConvertTo(pos: Int) {
        if (work) {
            return
        }
        convertToSelection = pos
        calcRate()
    }

    fun fireUpdatePurchase(isPurchase: Boolean) {
        if (work) {
            return
        }
        this.isPurchase = isPurchase
        calcRate()
    }

    override fun onGuiCreated(viewHost: ILab8View) {
        super.onGuiCreated(viewHost)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_8)
            isFirst = false
        }
        work = true
        view?.display(
            currency,
            currencySelection,
            convertToSelection,
            isPurchase,
            exchangeRateText,
            sum,
            result
        )
        work = false
    }
}