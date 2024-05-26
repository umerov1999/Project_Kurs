package dev.umerov.project.db.column

import android.provider.BaseColumns

object ShoppingListColumns : BaseColumns {
    const val TABLENAME = "shopping_list_column"

    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val CREATION_DATE = "creation_date"
    const val COLOR = "color"

    const val DB_PLANNED_PURCHASE = "db_planned_purchase"
    const val DB_PURCHASE = "db_purchase"

    const val FULL_ID = "$TABLENAME.${BaseColumns._ID}"
    const val FULL_TITLE = "$TABLENAME.$TITLE"
    const val FULL_DESCRIPTION = "$TABLENAME.$DESCRIPTION"
    const val FULL_CREATION_DATE = "$TABLENAME.$CREATION_DATE"
    const val FULL_COLOR = "$TABLENAME.$COLOR"
}
