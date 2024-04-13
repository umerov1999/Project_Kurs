package dev.umerov.project.db.column

import android.provider.BaseColumns

object CoinOperationsColumns : BaseColumns {
    const val TABLENAME = "coin_operations_column"

    const val DATE = "date"
    const val TITTLE = "tittle"
    const val COMMENT = "comment"
    const val TYPE = "type"
    const val COIN = "coin"
}