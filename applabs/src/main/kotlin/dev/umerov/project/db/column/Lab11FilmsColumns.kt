package dev.umerov.project.db.column

import android.provider.BaseColumns

object Lab11FilmsColumns : BaseColumns {
    const val TABLENAME = "lab11_films_column"

    const val TITLE = "title"
    const val YEAR = "year"
    const val GENRE_ID = "genre_id"
    const val THUMB_PATH = "thumb_path"

    const val FULL_ID = "$TABLENAME.${BaseColumns._ID}"
    const val FULL_TITLE = "$TABLENAME.$TITLE"
    const val FULL_YEAR = "$TABLENAME.$YEAR"
    const val FULL_GENRE_ID = "$TABLENAME.$GENRE_ID"
    const val FULL_THUMB_PATH = "$TABLENAME.$THUMB_PATH"
}
