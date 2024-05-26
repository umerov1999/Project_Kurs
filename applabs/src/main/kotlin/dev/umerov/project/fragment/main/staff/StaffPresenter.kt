package dev.umerov.project.fragment.main.staff

import android.os.Environment
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.kJson
import dev.umerov.project.model.Human
import dev.umerov.project.util.serializeble.json.decodeFromBufferedSource
import kotlinx.serialization.builtins.ListSerializer
import okio.buffer
import okio.source
import java.io.File
import java.io.FileOutputStream

class StaffPresenter : RxSupportPresenter<IStaffView>() {
    private var isFirst = true
    private val list = ArrayList<Human>()

    override fun onGuiCreated(viewHost: IStaffView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(list)
        if (isFirst) {
            viewHost.showMessage(R.string.staff)
            isFirst = false
        }
    }

    private fun backup() {
        val file = File(
            Environment.getExternalStorageDirectory(),
            "staff_data.json"
        )
        val bytes = kJson.encodeToString(ListSerializer(Human.serializer()), list).toByteArray(
            Charsets.UTF_8
        )
        val out = FileOutputStream(file)
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        out.write(bom)
        out.write(bytes)
        out.flush()
    }

    private fun restore() {
        val file = File(
            Environment.getExternalStorageDirectory(),
            "staff_data.json"
        )
        if (file.exists()) {
            list.clear()
            list.addAll(
                kJson.decodeFromBufferedSource(
                    ListSerializer(Human.serializer()),
                    file.source().buffer()
                )
            )
            view?.notifyDataSetChanged()
        }
    }

    fun fireStore(staff: Human) {
        if (staff.tempPosition < 0 || staff.tempPosition > list.size - 1) {
            staff.tempPosition = -1
            list.add(staff)
            view?.notifyDataAdded(list.size - 1, 1)
        } else {
            val pos = staff.tempPosition
            list[pos] = staff
            list[pos].tempPosition = -1
            view?.notifyItemChanged(pos)
        }
        backup()
    }

    fun fireMenuOpen(pos: Int, isOpen: Boolean) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        list[pos].tempIsMenuOpen = isOpen
        view?.notifyItemChanged(pos)
    }

    fun fireAdd() {
        view?.displayCreateDialog(Human())
    }

    fun fireDelete(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        list.removeAt(pos)
        view?.notifyDataRemoved(pos, 1)
        backup()
    }

    fun fireEdit(pos: Int) {
        if (pos < 0 || pos > list.size - 1) {
            return
        }
        list[pos].tempPosition = pos
        view?.displayCreateDialog(list[pos])
    }

    init {
        restore()
    }
}
