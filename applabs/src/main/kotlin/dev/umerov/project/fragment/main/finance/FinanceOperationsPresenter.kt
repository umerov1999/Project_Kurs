package dev.umerov.project.fragment.main.finance

import dev.umerov.project.Includes
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.fromIOToMain
import dev.umerov.project.model.main.labs.FinanceOperation

class FinanceOperationsPresenter(private val ownerId: Long) :
    RxSupportPresenter<IFinanceOperationsView>() {
    private val list = ArrayList<FinanceOperation>()

    override fun onGuiCreated(viewHost: IFinanceOperationsView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(list)
    }

    fun fireStore(item: FinanceOperation) {
        val oldId = item.db_id
        appendDisposable(
            Includes.stores.financeStore().updateOperation(item).fromIOToMain().subscribe({
                if (oldId < 0) {
                    list.add(0, item)
                    view?.notifyDataAdded(0, 1)
                } else {
                    for (i in list.indices) {
                        if (list[i].db_id == oldId) {
                            list[i] = item
                            view?.notifyItemChanged(i)
                            break
                        }
                    }
                }
            }, {
                view?.showThrowable(it)
            })
        )
    }

    fun fireAdd() {
        view?.displayCreateDialog(
            FinanceOperation().fetchColor().fetchCreateDate().setOwnerId(ownerId)
        )
    }

    fun fireDelete(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        appendDisposable(
            Includes.stores.financeStore().deleteOperation(list[pos].db_id).fromIOToMain()
                .subscribe({
                    list.removeAt(pos)
                    view?.notifyDataRemoved(pos, 1)
                }, {
                    view?.showThrowable(it)
                })
        )
    }

    fun fireEdit(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        view?.displayCreateDialog(list[pos])
    }

    fun fireClick(pos: Int) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation) {
            return
        }
        for (i in list.indices) {
            if (i != pos && list[i].tempIsEditMode) {
                list[i].tempIsEditMode = false
                list[i].tempIsAnimation = true
                view?.notifyItemChanged(i)
            } else if (i == pos) {
                list[i].tempIsEditMode = !list[i].tempIsEditMode
                list[i].tempIsAnimation = true
                view?.notifyItemChanged(i)
            }
        }
    }

    fun fireDeleteAnim(pos: Int) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation || !list[pos].tempIsEditMode) {
            return
        }
        appendDisposable(
            Includes.stores.financeStore().deleteOperation(list[pos].db_id).fromIOToMain()
                .subscribe({
                    list.removeAt(pos)
                    view?.notifyDataRemoved(pos, 1)
                }, {
                    view?.showThrowable(it)
                })
        )
    }

    fun fireEdit(
        pos: Int,
        title: String?,
        description: String?,
        coins: Double,
        isIncoming: Boolean
    ) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation || !list[pos].tempIsEditMode) {
            return
        }
        list[pos].setTitle(title)
        list[pos].setDescription(description)
        list[pos].setCoins(coins)
        list[pos].setIsIncome(isIncoming)
        list[pos].tempIsAnimation = true
        list[pos].tempIsEditMode = false
        fireStore(list[pos])
    }

    fun loadDb() {
        appendDisposable(
            Includes.stores.financeStore().getOperations(ownerId).fromIOToMain()
                .subscribe({
                    list.clear()
                    list.addAll(it)
                    view?.notifyDataSetChanged()
                }, { view?.showThrowable(it) })
        )
    }

    init {
        loadDb()
    }
}
