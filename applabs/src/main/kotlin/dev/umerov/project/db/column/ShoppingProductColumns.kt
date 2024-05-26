package dev.umerov.project.db.column

import android.provider.BaseColumns

object ShoppingProductColumns : BaseColumns {
    const val TABLENAME = "shopping_product_column"

    const val OWNER_ID = "owner_id"
    const val NAME = "name"
    const val COUNT = "count_item"
    const val IS_BOUGHT = "is_bought"
    const val UNIT = "unit"
    const val COLOR = "color"

    const val FULL_ID = "$TABLENAME.${BaseColumns._ID}"
    const val FULL_OWNER_ID = "$TABLENAME.$OWNER_ID"
    const val FULL_NAME = "$TABLENAME.$NAME"
    const val FULL_COUNT = "$TABLENAME.$COUNT"
    const val FULL_IS_BOUGHT = "$TABLENAME.$IS_BOUGHT"
    const val FULL_UNIT = "$TABLENAME.$UNIT"
    const val FULL_COLOR = "$TABLENAME.$COLOR"
}
