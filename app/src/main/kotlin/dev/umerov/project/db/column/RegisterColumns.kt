package dev.umerov.project.db.column

import android.provider.BaseColumns

object RegisterColumns : BaseColumns {
    const val TABLENAME = "register"

    const val COIN_BALANCE = "coin_balance"
    const val COIN_TAKED = "coin_taked"
    const val COIN_PASTED = "coin_pasted"
    const val OPERATIONS_COUNT = "operations_count"
}