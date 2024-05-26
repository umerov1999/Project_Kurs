package dev.umerov.project.db.column

import android.provider.BaseColumns

object Lab14PlaylistColumns : BaseColumns {
    const val TABLENAME = "lab14_playlist_column"

    const val TITLE = "title"
    const val YEAR = "year"
    const val ARTIST = "artist"
    const val COLOR = "color"
    const val THUMB_PATH = "thumb_path"

    const val FULL_ID = "$TABLENAME.${BaseColumns._ID}"
    const val FULL_TITLE = "$TABLENAME.$TITLE"
    const val FULL_YEAR = "$TABLENAME.$YEAR"
    const val FULL_ARTIST = "$TABLENAME.$ARTIST"
    const val FULL_COLOR = "$TABLENAME.$COLOR"
    const val FULL_THUMB_PATH = "$TABLENAME.$THUMB_PATH"
}
