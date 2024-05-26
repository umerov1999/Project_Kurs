package dev.umerov.project.fragment.main.lab14

import androidx.annotation.IdRes
import dev.umerov.project.Includes
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.fromIOToMain
import dev.umerov.project.model.main.labs.Lab14AudioAlbum

class Lab14Presenter : RxSupportPresenter<ILab14View>() {
    private val list = ArrayList<Lab14AudioAlbum>()
    private var tmpPos: Int? = null

    @IdRes
    private var checkedItem: Int = R.id.sort_by_name
    override fun onGuiCreated(viewHost: ILab14View) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(list)
        viewHost.updateCheckedItem(checkedItem)
    }

    fun fireChangeSelectItem(@IdRes checkedItem: Int) {
        this.checkedItem = checkedItem
        view?.updateCheckedItem(checkedItem)
        loadDb()
    }

    fun fireStore(playlist: Lab14AudioAlbum) {
        val oldId = playlist.db_id
        appendDisposable(
            Includes.stores.projectStore().updatePlaylist(playlist).fromIOToMain().subscribe({
                if (oldId < 0) {
                    list.add(0, playlist)
                    view?.notifyDataAdded(0, 1)
                } else {
                    for (i in list.indices) {
                        if (list[i].db_id == oldId) {
                            list[i] = playlist
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
        view?.displayCreateDialog(Lab14AudioAlbum().fetchColor())
    }

    fun fireDelete(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        appendDisposable(
            Includes.stores.projectStore().deletePlaylist(list[pos].db_id).fromIOToMain()
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
            Includes.stores.projectStore().deletePlaylist(list[pos].db_id).fromIOToMain()
                .subscribe({
                    list.removeAt(pos)
                    view?.notifyDataRemoved(pos, 1)
                }, {
                    view?.showThrowable(it)
                })
        )
    }

    fun fireEdit(pos: Int, title: String?, artist: String?, year: Int) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation || !list[pos].tempIsEditMode) {
            return
        }
        list[pos].setTitle(title).setArtist(artist).setYear(year)
        list[pos].tempIsAnimation = true
        list[pos].tempIsEditMode = false
        fireStore(list[pos])
    }

    fun fireSelectImage(pos: Int) {
        tmpPos = pos
    }

    fun fireOnSelectImage(thumb: String?) {
        tmpPos?.let {
            list[it].setThumbPath(thumb)
            list[it].tempIsAnimation = true
            list[it].tempIsEditMode = false
            fireStore(list[it])
            tmpPos = null
        }
    }

    private fun loadDb() {
        appendDisposable(
            Includes.stores.projectStore().getPlaylists(checkedItem).fromIOToMain()
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
