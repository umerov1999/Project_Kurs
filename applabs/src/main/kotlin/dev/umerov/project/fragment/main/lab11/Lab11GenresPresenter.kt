package dev.umerov.project.fragment.main.lab11

import dev.umerov.project.Includes
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.model.main.labs.Lab11Genre
import dev.umerov.project.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.umerov.project.util.coroutines.CoroutinesUtils.myEmit
import dev.umerov.project.view.Lab11GenreSelect

class Lab11GenresPresenter : RxSupportPresenter<ILab11GenresView>() {
    private val list = ArrayList<Lab11Genre>()
    override fun onGuiCreated(viewHost: ILab11GenresView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(list)
    }

    fun fireStore(genre: Lab11Genre) {
        val oldId = genre.db_id
        appendJob(
            Includes.stores.projectStore().updateGenre(genre).fromIOToMain({
                if (oldId < 0) {
                    list.add(0, genre)
                    view?.notifyDataAdded(0, 1)
                } else {
                    for (i in list.indices) {
                        if (list[i].db_id == oldId) {
                            list[i] = genre
                            view?.notifyItemChanged(i)
                            break
                        }
                    }
                }
                Lab11GenreSelect.changedObserve.myEmit(true)
            }, {
                view?.showThrowable(it)
            })
        )
    }

    fun fireAdd() {
        view?.displayCreateDialog(Lab11Genre())
    }

    fun fireDelete(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        appendJob(
            Includes.stores.projectStore().deleteGenre(list[pos].db_id).fromIOToMain({
                list.removeAt(pos)
                view?.notifyDataRemoved(pos, 1)
                Lab11GenreSelect.changedObserve.myEmit(true)
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
        appendJob(
            Includes.stores.projectStore().deleteGenre(list[pos].db_id).fromIOToMain({
                list.removeAt(pos)
                view?.notifyDataRemoved(pos, 1)
                Lab11GenreSelect.changedObserve.myEmit(true)
            }, {
                view?.showThrowable(it)
            })
        )
    }

    fun fireEdit(pos: Int, name: String?) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation || !list[pos].tempIsEditMode) {
            return
        }
        list[pos].setName(name)
        list[pos].tempIsAnimation = true
        list[pos].tempIsEditMode = false
        fireStore(list[pos])
    }

    init {
        appendJob(
            Includes.stores.projectStore().getGenres().fromIOToMain({
                list.clear()
                list.addAll(it)
                view?.notifyDataSetChanged()
            }, { view?.showThrowable(it) })
        )
    }
}
