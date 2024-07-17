package dev.umerov.project.fragment.main.finance

import dev.umerov.project.Includes
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.model.main.labs.FinanceBalance
import dev.umerov.project.util.coroutines.CoroutinesUtils.fromIOToMain

class BalancePresenter : RxSupportPresenter<IBalanceView>() {
    private var financeBalance: FinanceBalance? = null

    override fun onGuiCreated(viewHost: IBalanceView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(financeBalance)
    }

    fun fetch() {
        appendJob(Includes.stores.financeStore().fetchBalance()
            .fromIOToMain(
                {
                    financeBalance = it
                    view?.displayData(financeBalance)
                },
                {
                    view?.showThrowable(it)
                }
            )
        )
    }

    init {
        fetch()
    }
}
