package dev.umerov.project.fragment.main.finance

import dev.umerov.project.Includes
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.fromIOToMain
import dev.umerov.project.model.main.labs.FinanceWallet

class FinanceWalletsPresenter(private val isCreditCard: Boolean) :
    RxSupportPresenter<IFinanceWalletsView>() {
    private val list = ArrayList<FinanceWallet>()
    private var needReload = -1L

    override fun onGuiCreated(viewHost: IFinanceWalletsView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(list)
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        if (needReload >= 0) {
            loadDb()
        }
    }

    fun fireStore(item: FinanceWallet) {
        val oldId = item.db_id
        appendDisposable(
            Includes.stores.financeStore().updateWallet(item).fromIOToMain().subscribe({
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
            FinanceWallet().fetchColor().fetchCreateDate().setCreditCard(isCreditCard)
        )
    }

    fun fireDelete(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        appendDisposable(
            Includes.stores.financeStore().deleteWallet(list[pos].db_id).fromIOToMain()
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
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsEditMode) {
            return
        }
        needReload = list[pos].db_id
        view?.showOperationFragment(list[pos].db_id)
    }

    fun fireLongClick(pos: Int) {
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
            Includes.stores.financeStore().deleteWallet(list[pos].db_id).fromIOToMain()
                .subscribe({
                    list.removeAt(pos)
                    view?.notifyDataRemoved(pos, 1)
                }, {
                    view?.showThrowable(it)
                })
        )
    }

    fun fireEdit(pos: Int, title: String?) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation || !list[pos].tempIsEditMode) {
            return
        }
        list[pos].setTitle(title)
        list[pos].tempIsAnimation = true
        list[pos].tempIsEditMode = false
        fireStore(list[pos])
    }

    fun loadDb() {
        val tmpNeedReload = needReload
        needReload = -1L
        appendDisposable(
            Includes.stores.financeStore()
                .getWallets(isCreditCard, if (tmpNeedReload >= 0) tmpNeedReload else null)
                .fromIOToMain()
                .subscribe({
                    if (tmpNeedReload >= 0) {
                        if (it.isNotEmpty()) {
                            for (i in list.indices) {
                                if (list[i].db_id == tmpNeedReload) {
                                    list[i] = it[0]
                                    view?.notifyItemChanged(i)
                                    break
                                }
                            }
                        }
                    } else {
                        list.clear()
                        list.addAll(it)
                        view?.notifyDataSetChanged()
                    }
                }, { view?.showThrowable(it) })
        )
    }

    init {
        loadDb()
    }
}
