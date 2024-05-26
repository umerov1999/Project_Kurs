package dev.umerov.project.fragment.main

import android.os.Environment
import dev.umerov.project.R
import dev.umerov.project.fragment.base.RxSupportPresenter
import dev.umerov.project.model.main.MainButton
import dev.umerov.project.place.PlaceFactory
import java.io.File

class MainPresenter : RxSupportPresenter<IMainView>() {
    override fun onGuiCreated(viewHost: IMainView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(
            arrayOf(
                MainButton(PlaceFactory.getLAB_1Place()).setText(R.string.lab_1)
                    .setMainText(R.string.lesson_1),
                MainButton(PlaceFactory.getLAB_2Place()).setText(R.string.lab_2),
                MainButton(PlaceFactory.getLAB_3Place()).setText(R.string.lab_3),

                MainButton(PlaceFactory.getLAB_4Place()).setText(R.string.lab_4)
                    .setMainText(R.string.lesson_2),
                MainButton(PlaceFactory.getLAB_4_1Place()).setText(R.string.lab_4_1),
                MainButton(PlaceFactory.getLAB_5Place()).setText(R.string.lab_5),
                MainButton(PlaceFactory.getLAB_6Place()).setText(R.string.lab_6),
                MainButton(PlaceFactory.getLAB_7Place()).setText(R.string.lab_7),
                MainButton(PlaceFactory.getLAB_8Place()).setText(R.string.lab_8),

                MainButton(PlaceFactory.getStaffPlace()).setText(R.string.staff)
                    .setMainText(R.string.lesson_3),
                MainButton(
                    PlaceFactory.getFileManagerPlace(
                        (if (Environment.getExternalStorageDirectory().isDirectory && Environment.getExternalStorageDirectory()
                                .canRead()
                        ) Environment.getExternalStorageDirectory() else File("/")).absolutePath,
                        base = false,
                        isSelect = false
                    )
                ).setText(R.string.file_manager),

                MainButton(PlaceFactory.getLAB_9Place()).setText(R.string.lab_9)
                    .setMainText(R.string.lesson_4),
                MainButton(PlaceFactory.getLAB_10Place()).setText(R.string.lab_10),
                MainButton(PlaceFactory.getLAB_11Place()).setText(R.string.lab_11),

                MainButton(PlaceFactory.getLAB_12Place()).setText(R.string.lab_12)
                    .setMainText(R.string.lesson_5),
                MainButton(PlaceFactory.getLAB_13Place()).setText(R.string.lab_13),

                MainButton(PlaceFactory.getLAB_14Place()).setText(R.string.lab_14)
                    .setMainText(R.string.lesson_6),

                MainButton(PlaceFactory.getLAB_15Place()).setText(R.string.lab_15)
                    .setMainText(R.string.lesson_7),
                MainButton(PlaceFactory.getLAB_16Place()).setText(R.string.lab_16),
                MainButton(PlaceFactory.getLAB_17Place()).setText(R.string.lab_17),
                MainButton(PlaceFactory.getLAB_18Place()).setText(R.string.lab_18),

                MainButton(PlaceFactory.getLAB_19Place()).setText(R.string.lab_19)
                    .setMainText(R.string.lesson_8),

                MainButton(PlaceFactory.getSnakePlace()).setText(R.string.snake)
                    .setMainText(R.string.lesson_9),

                MainButton(PlaceFactory.getShoppingListPlace()).setText(R.string.shopping_list)
                    .setMainText(R.string.final_work)
            )
        )
    }

    fun fireClickedButton(btn: MainButton) {
        btn.place?.let { view?.openPlace(it) }
    }
}