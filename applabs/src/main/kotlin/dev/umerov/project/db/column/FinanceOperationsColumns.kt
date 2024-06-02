package dev.umerov.project.db.column

import android.provider.BaseColumns

object FinanceOperationsColumns : BaseColumns {
    const val TABLENAME = "finance_operations_column"

    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val CREATED_DATE = "created_date"
    const val COLOR = "color"
    const val IS_INCOME = "is_income"
    const val COINS = "coins"
    const val OWNER_ID = "owner_id"

    const val FULL_ID = "$TABLENAME.${BaseColumns._ID}"
    const val FULL_OWNER_ID = "$TABLENAME.$OWNER_ID"
    const val FULL_TITLE = "$TABLENAME.$TITLE"
    const val FULL_CREATED_DATE = "$TABLENAME.$CREATED_DATE"
    const val FULL_COLOR = "$TABLENAME.$COLOR"
    const val FULL_IS_INCOME = "$TABLENAME.$IS_INCOME"
    const val FULL_COINS = "$TABLENAME.$COINS"
}
