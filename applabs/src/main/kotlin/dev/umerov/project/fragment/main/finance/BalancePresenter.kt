package dev.umerov.project.fragment.main.finance

import dev.umerov.project.Includes
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.fromIOToMain
import dev.umerov.project.model.main.labs.FinanceBalance

class BalancePresenter : RxSupportPresenter<IBalanceView>() {
    private var financeBalance: FinanceBalance? = null

    override fun onGuiCreated(viewHost: IBalanceView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(financeBalance)
    }

    fun fetch() {
        appendDisposable(Includes.stores.financeStore().fetchBalance()
            .fromIOToMain()
            .subscribe(
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
