package dev.umerov.project.fragment.main.coin

import dev.umerov.project.Includes
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.fromIOToMain
import dev.umerov.project.model.db.CoinOperation
import dev.umerov.project.model.db.CoinOperationType
import dev.umerov.project.model.db.Register
import dev.umerov.project.model.db.RegisterType
import dev.umerov.project.util.Utils
import java.util.Calendar

class CoinPresenter(@CoinOperationType private val operationType: Int) :
    RxSupportPresenter<ICoinView>() {
    private val operations: MutableList<CoinOperation> = ArrayList()
    private var register: Register? = null
    private var actualDataLoading = false

    override fun onGuiCreated(viewHost: ICoinView) {
        super.onGuiCreated(viewHost)
        viewHost.displayList(operations)
        viewHost.displayRegister(register)
    }

    fun fireRefresh() {
        if (actualDataLoading) {
            return
        }
        loadActualData()
    }

    private fun loadActualData() {
        actualDataLoading = true
        resolveRefreshingView()
        appendDisposable(Includes.stores.projectStore().fetchCoinOperations(operationType)
            .fromIOToMain()
            .subscribe({
                onActualDataReceived(
                    it
                )
            }) { t -> onActualDataGetError(t) })
    }

    private fun onActualDataGetError(t: Throwable) {
        actualDataLoading = false
        view?.let {
            showError(
                it,
                Utils.getCauseIfRuntime(t)
            )
        }
        resolveRefreshingView()
    }

    private fun onActualDataReceived(data: List<CoinOperation>) {
        actualDataLoading = false
        operations.clear()
        operations.addAll(data)
        view?.notifyListChanged()
        resolveRefreshingView()
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        view?.displayLoading(
            actualDataLoading
        )
    }

    fun fireStore(operation: CoinOperation) {
        if (operation.dbId != -1L) {
            var found = false
            var pos = 0
            for (i in operations) {
                if (i.dbId == operation.dbId) {
                    found = true
                    appendDisposable(Includes.stores.projectStore().updateOperation(operation)
                        .fromIOToMain()
                        .subscribe(
                            {
                                operations[pos] = operation
                                view?.notifyItemChanged(pos)
                                Includes.needReadRegister.onNext(true)
                            },
                            {
                                view?.showThrowable(it)
                            }
                        )
                    )
                    break
                }
                pos++
            }
            if (!found) {
                appendDisposable(Includes.stores.projectStore().updateOperation(operation)
                    .fromIOToMain()
                    .subscribe(
                        {
                            Includes.needReadRegister.onNext(true)
                            loadActualData()
                        },
                        {
                            view?.showThrowable(it)
                        }
                    )
                )
            }
        } else {
            appendDisposable(Includes.stores.projectStore().addOperation(operation)
                .fromIOToMain()
                .subscribe(
                    {
                        Includes.needReadRegister.onNext(true)
                        operations.add(0, operation)
                        view?.notifyDataAdded(0, 1)
                        view?.notifyItemChanged(1)
                    },
                    {
                        view?.showThrowable(it)
                    }
                )
            )
        }
    }

    fun fireDelete(pos: Int) {
        if (operations.size <= pos || pos < 0) {
            return
        }
        appendDisposable(Includes.stores.projectStore().removeOperation(operations[pos].dbId)
            .fromIOToMain()
            .subscribe(
                {
                    operations.removeAt(pos)
                    view?.notifyDataRemoved(pos, 1)
                    Includes.needReadRegister.onNext(true)
                },
                {
                    view?.showThrowable(it)
                }
            )
        )
    }

    fun fireAdd() {
        val coinOperation = CoinOperation(
            Calendar.getInstance().timeInMillis / 1000,
            null,
            null,
            operationType,
            0.0
        )
        view?.displayCreateDialog(coinOperation)
    }

    fun fireEdit(pos: Int) {
        if (operations.size <= pos || pos < 0) {
            return
        }
        view?.displayCreateDialog(operations[pos])
    }

    private fun fetchRegister() {
        appendDisposable(Includes.stores.projectStore().fetchRegister(RegisterType.BALANCE)
            .fromIOToMain()
            .subscribe(
                {
                    register = it
                    view?.displayRegister(register)
                },
                {
                    view?.showThrowable(it)
                }
            )
        )
    }

    init {
        fetchRegister()
        loadActualData()
        appendDisposable(Includes.needReadRegister.subscribe {
            fetchRegister()
        })
    }
}
