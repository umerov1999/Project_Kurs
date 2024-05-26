package dev.umerov.project.fragment.main.shoppingproducts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.umerov.project.Extra
import dev.umerov.project.Includes
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.fromIOToMain
import dev.umerov.project.model.main.labs.Product
import dev.umerov.project.model.main.labs.ProductUnit
import dev.umerov.project.model.main.labs.ShoppingList
import dev.umerov.project.util.NotificationHelper
import dev.umerov.project.util.Utils
import dev.umerov.project.util.serializeble.msgpack.MsgPack


class ShoppingProductsPresenter(
    private val context: Context,
    private val shoppingList: ShoppingList
) :
    RxSupportPresenter<IShoppingProductsView>() {
    private val list = ArrayList<Product>()
    override fun onGuiCreated(viewHost: IShoppingProductsView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(list)
    }

    fun fireScheduleNotification(time: Long) {
        try {
            val intent = Intent(context, NotificationHelper::class.java)
            intent.putExtra(
                Extra.DATA,
                MsgPack.encodeToByteArrayEx(ShoppingList.serializer(), shoppingList)
            )
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                Utils.makeMutablePendingIntent(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.set(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
            view?.showMessage(R.string.success)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fireStore(product: Product, boughtToggle: Boolean) {
        val oldId = product.db_id
        appendDisposable(
            Includes.stores.shoppingStore().updateProduct(product).fromIOToMain()
                .subscribe({
                    if (oldId < 0) {
                        if (product.isBought) {
                            val tmpSize = list.size
                            list.add(list.size, product)
                            view?.notifyDataAdded(tmpSize, 1)
                        } else {
                            list.add(0, product)
                            view?.notifyDataAdded(0, 1)
                        }
                    } else {
                        for (i in list.indices) {
                            if (list[i].db_id == oldId && !boughtToggle) {
                                list[i] = product
                                view?.notifyItemChanged(i)
                                break
                            } else if (list[i].db_id == oldId) {
                                if (list[i].isBought) {
                                    val tmp = list.removeAt(i)
                                    list.add(list.size, tmp)
                                    view?.notifyItemMoved(i, (list.size - 1).coerceAtLeast(0))
                                    view?.notifyItemChanged((list.size - 1).coerceAtLeast(0))
                                } else {
                                    val tmp = list.removeAt(i)
                                    list.add(0, tmp)
                                    view?.notifyItemMoved(i, 0)
                                    view?.notifyItemChanged(0)
                                }
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
        view?.displayCreateDialog(Product().fetchColor().setDBOwnerId(shoppingList.db_id))
    }

    fun fireDelete(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        appendDisposable(
            Includes.stores.shoppingStore().deleteProduct(list[pos].db_id).fromIOToMain()
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
            Includes.stores.shoppingStore().deleteProduct(list[pos].db_id).fromIOToMain()
                .subscribe({
                    list.removeAt(pos)
                    view?.notifyDataRemoved(pos, 1)
                }, {
                    view?.showThrowable(it)
                })
        )
    }

    fun fireBought(pos: Int, isBought: Boolean) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        val tmpBoughtToggle = list[pos].isBought != isBought
        if (tmpBoughtToggle) {
            list[pos].setIsBought(isBought)
            fireStore(list[pos], true)
        }
    }

    fun fireEdit(pos: Int, name: String?, count: Int, @ProductUnit unit: Int) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation || !list[pos].tempIsEditMode) {
            return
        }
        list[pos].setName(name).setCount(count).setUnit(unit)
        list[pos].tempIsAnimation = true
        list[pos].tempIsEditMode = false
        fireStore(list[pos], false)
    }

    fun loadDb() {
        appendDisposable(
            Includes.stores.shoppingStore()
                .getProducts(shoppingList.db_id).fromIOToMain()
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
