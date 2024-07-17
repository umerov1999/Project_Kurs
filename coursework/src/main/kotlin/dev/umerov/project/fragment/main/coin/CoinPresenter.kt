package dev.umerov.project.fragment.main.coin

import dev.umerov.project.Includes
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.model.db.CoinOperation
import dev.umerov.project.model.db.CoinOperationType
import dev.umerov.project.model.db.Register
import dev.umerov.project.model.db.RegisterType
import dev.umerov.project.util.Utils
import dev.umerov.project.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.umerov.project.util.coroutines.CoroutinesUtils.myEmit
import dev.umerov.project.util.coroutines.CoroutinesUtils.sharedFlowToMain
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
        appendJob(Includes.stores.projectStore().fetchCoinOperations(operationType)
            .fromIOToMain({
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
                    appendJob(Includes.stores.projectStore().updateOperation(operation)
                        .fromIOToMain(
                            {
                                operations[pos] = operation
                                view?.notifyItemChanged(pos)
                                Includes.needReadRegister.myEmit(true)
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
                appendJob(Includes.stores.projectStore().updateOperation(operation)
                    .fromIOToMain(
                        {
                            Includes.needReadRegister.myEmit(true)
                            loadActualData()
                        },
                        {
                            view?.showThrowable(it)
                        }
                    )
                )
            }
        } else {
            appendJob(Includes.stores.projectStore().addOperation(operation)
                .fromIOToMain(
                    {
                        Includes.needReadRegister.myEmit(true)
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
        appendJob(Includes.stores.projectStore().removeOperation(operations[pos].dbId)
            .fromIOToMain(
                {
                    operations.removeAt(pos)
                    view?.notifyDataRemoved(pos, 1)
                    Includes.needReadRegister.myEmit(true)
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
        appendJob(Includes.stores.projectStore().fetchRegister(RegisterType.BALANCE)
            .fromIOToMain(
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
        appendJob(Includes.needReadRegister.sharedFlowToMain {
            fetchRegister()
        })
    }
}
