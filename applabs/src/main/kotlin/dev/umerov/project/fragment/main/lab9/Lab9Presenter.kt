package dev.umerov.project.fragment.main.lab9

import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.model.main.labs.Lab9Contact

class Lab9Presenter : RxSupportPresenter<ILab9View>() {
    private var isFirst = true
    private val list = ArrayList<Lab9Contact>()
    override fun onGuiCreated(viewHost: ILab9View) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(list)
        if (isFirst) {
            viewHost.showMessage(R.string.lab_9)
            isFirst = false
        }
    }

    fun fireStore(contact: Lab9Contact) {
        if (contact.tempPosition < 0 || contact.tempPosition > list.size - 1) {
            contact.tempPosition = -1
            list.add(contact)
            view?.notifyDataAdded(list.size - 1, 1)
        } else {
            val pos = contact.tempPosition
            list[pos] = contact
            list[pos].tempPosition = -1
            view?.notifyItemChanged(pos)
        }
    }

    fun fireAdd() {
        view?.displayCreateDialog(Lab9Contact())
    }

    fun fireDelete(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        list.removeAt(pos)
        view?.notifyDataRemoved(pos, 1)
    }

    fun fireEdit(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        list[pos].tempPosition = pos
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
        list.removeAt(pos)
        view?.notifyDataRemoved(pos, 1)
    }

    fun fireEdit(pos: Int, contact: String?, mail: String?) {
        if (pos < 0 || pos > list.size - 1 || list[pos].tempIsAnimation || !list[pos].tempIsEditMode) {
            return
        }
        list[pos].email = mail
        list[pos].contact = contact
        list[pos].tempIsAnimation = true
        list[pos].tempIsEditMode = false
        view?.notifyItemChanged(pos)
    }
}
