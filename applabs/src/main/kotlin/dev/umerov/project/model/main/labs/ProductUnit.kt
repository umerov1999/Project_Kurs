package dev.umerov.project.model.main.labs

import androidx.annotation.IntDef
import androidx.annotation.StringRes
import dev.umerov.project.R

@IntDef(
    ProductUnit.PIECES,
    ProductUnit.LITERS,
    ProductUnit.KILOGRAMS,
    ProductUnit.GRAMS,
    ProductUnit.METERS
)
@Retention(AnnotationRetention.SOURCE)
annotation class ProductUnit {
    companion object {
        const val PIECES = 0
        const val LITERS = 1
        const val KILOGRAMS = 2
        const val GRAMS = 3
        const val METERS = 4

        @StringRes
        fun toStringRes(@ProductUnit unit: Int): Int {
            return when (unit) {
                GRAMS -> {
                    R.string.grams
                }

                KILOGRAMS -> {
                    R.string.kilograms
                }

                LITERS -> {
                    R.string.liters
                }

                METERS -> {
                    R.string.meters
                }

                PIECES -> {
                    R.string.pieces
                }

                else -> {
                    throw UnsupportedOperationException()
                }
            }
        }
    }
}
