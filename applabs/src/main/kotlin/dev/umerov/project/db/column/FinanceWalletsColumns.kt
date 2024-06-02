package dev.umerov.project.db.column

import android.provider.BaseColumns

object FinanceWalletsColumns : BaseColumns {
    const val TABLENAME = "finance_wallet_column"

    const val TITLE = "title"
    const val CREATED_DATE = "created_date"
    const val COLOR = "color"
    const val IS_CREDIT_CARD = "is_credit_card"
    const val COINS_SUM_IN = "coins_sum_in"
    const val COINS_SUM_OUT = "coins_sum_out"

    const val FULL_ID = "$TABLENAME.${BaseColumns._ID}"
    const val FULL_TITLE = "$TABLENAME.$TITLE"
    const val FULL_CREATED_DATE = "$TABLENAME.$CREATED_DATE"
    const val FULL_COLOR = "$TABLENAME.$COLOR"
    const val FULL_IS_CREDIT_CARD = "$TABLENAME.$IS_CREDIT_CARD"
}
