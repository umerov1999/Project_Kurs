package dev.umerov.project.fragment.main.balance

import dev.umerov.project.Includes
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.fromIOToMain
import dev.umerov.project.model.db.Register
import dev.umerov.project.model.db.RegisterType

class BalancePresenter : RxSupportPresenter<IBalanceView>() {
    private var register: Register? = null

    override fun onGuiCreated(viewHost: IBalanceView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(register)
    }

    private fun fetchRegister() {
        appendDisposable(Includes.stores.projectStore().fetchRegister(RegisterType.BALANCE)
            .fromIOToMain()
            .subscribe(
                {
                    register = it
                    view?.displayData(register)
                },
                {
                    view?.showThrowable(it)
                }
            )
        )
    }

    init {
        fetchRegister()
        appendDisposable(Includes.needReadRegister.subscribe {
            fetchRegister()
        })
    }
}
