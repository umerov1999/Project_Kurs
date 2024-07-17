package dev.umerov.project.fragment.main.lab11

import dev.umerov.project.Includes
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.model.main.labs.Lab11Film
import dev.umerov.project.model.main.labs.Lab11Genre
import dev.umerov.project.util.coroutines.CoroutinesUtils.fromIOToMain

class Lab11FilmsPresenter : RxSupportPresenter<ILab11FilmsView>() {
    private val list = ArrayList<Lab11Film>()
    private var tmpPos: Int? = null
    override fun onGuiCreated(viewHost: ILab11FilmsView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(list)
    }

    fun fireStore(film: Lab11Film) {
        val oldId = film.db_id
        appendJob(
            Includes.stores.projectStore().updateFilm(film).fromIOToMain({
                if (oldId < 0) {
                    list.add(0, film)
                    view?.notifyDataAdded(0, 1)
                } else {
                    for (i in list.indices) {
                        if (list[i].db_id == oldId) {
                            list[i] = film
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
        view?.displayCreateDialog(Lab11Film())
    }

    fun fireDelete(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        appendJob(
            Includes.stores.projectStore().deleteFilm(list[pos].db_id).fromIOToMain({
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
        appendJob(
            Includes.stores.projectStore().deleteFilm(list[pos].db_id).fromIOToMain({
                list.removeAt(pos)
                view?.notifyDataRemoved(pos, 1)
            }, {
                view?.showThrowable(it)
            })
        )
    }

    fun fireEdit(pos: Int, title: String?, genre: Lab11Genre?, year: Int) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation || !list[pos].tempIsEditMode) {
            return
        }
        list[pos].setTitle(title).setGenre(genre).setYear(year)
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

    init {
        appendJob(
            Includes.stores.projectStore().getFilms().fromIOToMain({
                list.clear()
                list.addAll(it)
                view?.notifyDataSetChanged()
            }, { view?.showThrowable(it) })
        )
    }
}
