package dev.umerov.project.db.column

import android.provider.BaseColumns

object Lab11GenresColumns : BaseColumns {
    const val TABLENAME = "lab11_genres_column"

    const val NAME = "name"

    const val FULL_ID = "$TABLENAME.${BaseColumns._ID}"
    const val FULL_NAME = "$TABLENAME.$NAME"
}
