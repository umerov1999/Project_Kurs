package dev.umerov.project.fragment.main.shoppinglist

import dev.umerov.project.Includes
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.fromIOToMain
import dev.umerov.project.model.main.labs.ShoppingList

class ShoppingListPresenter : RxSupportPresenter<IShoppingListView>() {
    private val list = ArrayList<ShoppingList>()
    private var needReload = -1L
    override fun onGuiCreated(viewHost: IShoppingListView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(list)
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        if (needReload >= 0) {
            loadDb()
        }
    }

    fun fireStore(shoppingList: ShoppingList) {
        val oldId = shoppingList.db_id
        appendDisposable(
            Includes.stores.shoppingStore().updateShoppingList(shoppingList).fromIOToMain()
                .subscribe({
                    if (oldId < 0) {
                        list.add(0, shoppingList)
                        view?.notifyDataAdded(0, 1)
                    } else {
                        for (i in list.indices) {
                            if (list[i].db_id == oldId) {
                                list[i] = shoppingList
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
        view?.displayCreateDialog(ShoppingList().fetchColor())
    }

    fun fireDelete(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        appendDisposable(
            Includes.stores.shoppingStore().deleteShoppingList(list[pos].db_id).fromIOToMain()
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

    fun fireExpand(pos: Int) {
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

    fun fireClick(pos: Int) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsEditMode) {
            return
        }
        needReload = list[pos].db_id
        view?.showProductFragment(list[pos])
    }

    fun fireDeleteAnim(pos: Int) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation || !list[pos].tempIsEditMode) {
            return
        }
        appendDisposable(
            Includes.stores.shoppingStore().deleteShoppingList(list[pos].db_id).fromIOToMain()
                .subscribe({
                    list.removeAt(pos)
                    view?.notifyDataRemoved(pos, 1)
                }, {
                    view?.showThrowable(it)
                })
        )
    }

    fun fireEdit(pos: Int, title: String?, description: String?) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation || !list[pos].tempIsEditMode) {
            return
        }
        list[pos].setTitle(title).setDescription(description)
        list[pos].tempIsAnimation = true
        list[pos].tempIsEditMode = false
        fireStore(list[pos])
    }

    fun loadDb() {
        val tmpNeedReload = needReload
        needReload = -1L
        appendDisposable(
            Includes.stores.shoppingStore()
                .getShoppingList(if (tmpNeedReload >= 0) tmpNeedReload else null).fromIOToMain()
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